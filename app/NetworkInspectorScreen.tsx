import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, Alert, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { WebView, WebViewMessageEvent } from 'react-native-webview';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { saveM3U8Link } from '../utils/storage';

// Params passed via navigation
interface NetworkInspectorParams {
  channelId?: string;
  pageUrl?: string;
}

// Build the injected script that intercepts fetch and XHR
const buildInjectedScript = (): string => `
  (function() {
    if (!window.ReactNativeWebView || !window.ReactNativeWebView.postMessage) {
      return true;
    }

    var reported = false;

    function isStrongM3U8(url) {
      try {
        if (typeof url !== 'string') return false;
        if (!url.includes('.m3u8')) return false;
        var lower = url.toLowerCase();
        // Prefer real playlist URLs, not tracking/error endpoints
        if (lower.endsWith('master.m3u8') || lower.endsWith('playlist.m3u8')) return true;
        // Whitelist known hosts (Bloomberg, CNN, ABC etc.)
        if (
          lower.includes('dai.google.com') ||
          lower.includes('cdn.livenewsplayer.com') ||
          lower.includes('abc') ||
          lower.includes('bloomberg') ||
          lower.includes('cnn')
        ) return true;
        return false;
      } catch (e) {
        return false;
      }
    }

    function reportIfM3U8(url) {
      try {
        if (typeof url !== 'string') return;
        if (url.indexOf('.m3u8') === -1) return;
        if (!/^https?:\\/\\//i.test(url)) return;

        // Always send candidates to React Native for inspection
        window.ReactNativeWebView.postMessage(JSON.stringify({
          type: 'M3U8_FOUND',
          url: url
        }));

        // Only stop further scanning once we see a strong candidate
        if (!reported && isStrongM3U8(url)) {
          reported = true;
        }
      } catch (e) {}
    }

    // Patch fetch
    if (typeof window.fetch === 'function') {
      var originalFetch = window.fetch;
      window.fetch = function() {
        var args = Array.prototype.slice.call(arguments);
        var url = args[0];
        if (typeof url === 'string') {
          reportIfM3U8(url);
        } else if (url && typeof url.url === 'string') {
          reportIfM3U8(url.url);
        }
        return originalFetch.apply(this, args).then(function(response) {
          try {
            if (response && response.url) {
              reportIfM3U8(response.url);
            }
          } catch (e) {}
          return response;
        });
      };
    }

    // Patch XHR
    if (window.XMLHttpRequest) {
      var originalOpen = XMLHttpRequest.prototype.open;
      XMLHttpRequest.prototype.open = function(method, url) {
        try {
          if (typeof url === 'string') {
            reportIfM3U8(url);
          }
        } catch (e) {}
        return originalOpen.apply(this, arguments);
      };
    }

    true;
  })();
`;

// Try to normalize common wrapped/tracking URLs into the underlying playlist
const extractRealM3U8 = (inputUrl: string): string | null => {
  try {
    const urlObj = new URL(inputUrl);

    // Common patterns used by sites like ABC / CNN / aggregators:
    const possibleParams = ['param8', 'url', 'streamUrl'];
    for (const key of possibleParams) {
      const value = urlObj.searchParams.get(key);
      if (value && value.includes('.m3u8')) {
        return decodeURIComponent(value);
      }
    }

    // Fallback: scan all params for an embedded .m3u8
    for (const [_, value] of urlObj.searchParams.entries()) {
      if (value.includes('.m3u8')) {
        return decodeURIComponent(value);
      }
    }

  } catch (e) {
    // ignore parse errors
  }

  return null;
};

// Ensure we only store a real playlist URL
const isValidPlaylist = (url: string | null): boolean => {
  if (!url) return false;
  if (!/^https?:\/\//i.test(url)) return false;
  if (!url.includes('.m3u8')) return false;
  // Hard reject known tracking domains like youboranqs ping endpoints
  const lower = url.toLowerCase();
  if (lower.includes('youboranqs') || lower.includes('/ping?')) return false;
  return true;
};

const SCAN_TIMEOUT_MS = 25000; // 20s max scan window

const NetworkInspectorScreen: React.FC = () => {
  const params = useLocalSearchParams();
  const channelId = params.channelId as string | undefined;
  const pageUrl = params.pageUrl as string | undefined;
  const router = useRouter();
  const [hasCaptured, setHasCaptured] = useState(false);
  const [timedOut, setTimedOut] = useState(false);

  const injectedJavaScriptBeforeContentLoaded = useMemo(buildInjectedScript, []);

  // Timeout: if nothing is captured within SCAN_TIMEOUT_MS, inform user
  useEffect(() => {
    if (!channelId || !pageUrl) return;
    if (hasCaptured) return;

    const timer = setTimeout(() => {
      if (!hasCaptured) {
        setTimedOut(true);
        // console.log('[Inspector] Scan timed out without finding .m3u8');
      }
    }, SCAN_TIMEOUT_MS);

    return () => clearTimeout(timer);
  }, [channelId, pageUrl, hasCaptured]);

  const onMessage = useCallback(
    async (event: WebViewMessageEvent) => {
      if (hasCaptured) return;

      try {
        const data = event.nativeEvent.data;
        // console.log('[Inspector] Raw message from WebView:', data);
        let parsed: any = null;

        // Try JSON first
        try {
          parsed = JSON.parse(data);
        } catch {
          if (typeof data === 'string' && data.includes('.m3u8')) {
            parsed = { type: 'M3U8_FOUND', url: data };
          }
        }

        if (!parsed || parsed.type !== 'M3U8_FOUND' || typeof parsed.url !== 'string') {
          return;
        }

        const rawUrl = parsed.url.trim();
        // console.log('[Inspector] Parsed M3U8 candidate (raw):', rawUrl);

        // Try to unwrap tracking URLs; if nothing found, use the raw URL directly
        const finalUrl = extractRealM3U8(rawUrl) || rawUrl;
        // console.log('[Inspector] Normalized M3U8 (final):', finalUrl);

        if (!isValidPlaylist(finalUrl)) {
          // console.log('[Inspector] Rejected URL (not valid playable m3u8):', finalUrl);
          return;
        }

        if (!channelId) {
          console.warn('[Inspector] Missing channelId, cannot save m3u8 URL');
          return;
        }

        setHasCaptured(true);
        // console.log('[Inspector] Saving M3U8 for channel', String(channelId), '=>', finalUrl);
        await saveM3U8Link(String(channelId), finalUrl as string);

        Alert.alert('Stream updated', 'New stream URL found.', [
          {
            text: 'OK',
            onPress: () => {
              if (router.canGoBack()) router.back();
              else router.replace('/');
            },
          },
        ]);
      } catch (error) {
        // console.warn('[Inspector] Error handling WebView message', error);
      }
    },
    [channelId, hasCaptured, router]
  );

  const isFullscreenWebOnlyChannel = (channelId?: string) => ['7', '8', '9'].includes(channelId || '');

  const fullscreenWebOnly = isFullscreenWebOnlyChannel(channelId);

  // For specific US channels (ABC, Yahoo Finance, CNN Live),
  // use fullscreen WebView as primary player and hide timeout warning.

  if (!channelId || !pageUrl) {
    return (
      <SafeAreaView style={styles.center}>
        <Text style={styles.errorText}>Missing channelId or pageUrl.</Text>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={fullscreenWebOnly ? styles.abcContainer : styles.container}>
      {!fullscreenWebOnly && (
        <View style={styles.infoContainer}>
          <Text style={styles.title}>Play the stream to capture URL</Text>
          <Text style={styles.subtitle}>Channel: {channelId}</Text>
          <Text style={styles.subtitle}>We will auto-detect the .m3u8 while you watch.</Text>
          {timedOut && !hasCaptured && (
            <Text style={[styles.subtitle, { color: '#f97316' }]}>No .m3u8 detected yet. This channel may use protected playback.</Text>
          )}
        </View>
      )}

      <View style={styles.webviewContainer}>
        <WebView
          source={{ uri: String(pageUrl) }}
          onMessage={onMessage}
          injectedJavaScriptBeforeContentLoaded={injectedJavaScriptBeforeContentLoaded}
          javaScriptEnabled
          domStorageEnabled
          allowsInlineMediaPlayback
          mediaPlaybackRequiresUserAction={false}
          style={styles.visibleWebView}
        />
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#050816',
    paddingHorizontal: 16,
    paddingTop: 16,
  },
  abcContainer: {
    flex: 1,
    backgroundColor: '#000',
  },
  infoContainer: {
    marginBottom: 12,
  },
  title: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 4,
  },
  subtitle: {
    color: '#9ca3af',
    fontSize: 13,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginTop: 8,
  },
  webviewContainer: {
    flex: 1,
    borderRadius: 12,
    overflow: 'hidden',
    marginTop: 8,
  },
  visibleWebView: {
    width: '100%',
    height: '100%',
  },
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#050816',
  },
  errorText: {
    color: '#f87171',
    fontSize: 16,
  },
});

export default NetworkInspectorScreen;
