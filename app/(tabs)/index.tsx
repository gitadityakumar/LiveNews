import React, { useCallback, useState, useEffect } from 'react';
import { StyleSheet, View, Pressable, Text } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import VideoPlayerSection from '@/components/VideoPlayerSection';
import NewsChannelList from '@/components/NewsChannelList';
import { STREAMS, NEWS_CHANNELS } from '@/constants/data';
import { fullscreenEmitter } from './_layout';
import { getM3U8Link } from '@/utils/storage';
import { useRouter, useFocusEffect } from 'expo-router';

export default function LiveNews() {
  const router = useRouter();
  const [region, setRegion] = useState<'india' | 'usa'>('india');
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [currentStreamIndex, setCurrentStreamIndex] = useState(0);
  const [currentStreamUrl, setCurrentStreamUrl] = useState<string | null>(STREAMS['india'][0]);

  useEffect(() => {
    setCurrentStreamIndex(0);
    setCurrentStreamUrl(STREAMS[region][0]);
  }, [region]);

  const resolveStreamUrl = async (regionKey: 'india' | 'usa', streamIndex: number): Promise<string | null> => {
    const base = STREAMS[regionKey][streamIndex] ?? null;

    // For US channels, prefer cached dynamic .m3u8 if available
    if (regionKey === 'usa') {
      const channel = NEWS_CHANNELS.usa.find((c) => c.streamIndex === streamIndex);
      if (channel) {
        const cached = await getM3U8Link(String(channel.id));
        if (cached && cached.includes('.m3u8')) return cached;
      }
    }

    return base;
  };

  const handleChannelSelect = async (streamIndex: number) => {
    setCurrentStreamIndex(streamIndex);
    const url = await resolveStreamUrl(region, streamIndex);
    if (url) setCurrentStreamUrl(url);
  };

  const handleReloadChannel = (channelId: number) => {
    // Map channel id to page URL. For India we skip; for US we provide.
    const usChannel = NEWS_CHANNELS.usa.find((c) => c.id === channelId);
    if (!usChannel) return;

    // Example mapping - customize with real page URLs
    let pageUrl: string | null = null;
    if (usChannel.name.includes('Bloomberg')) pageUrl = 'https://www.bloomberg.com/live/us';
    else if (usChannel.name.includes('ABC News')) pageUrl = 'https://www.livenewsnow.com/american/abc-news-2.html';
    else if (usChannel.name.includes('Yahoo Finance')) pageUrl = 'https://finance.yahoo.com/live';
    else if (usChannel.name.includes('CNN')) pageUrl = 'https://www.livenewsnow.com/american/cnn-live-free.html';
    else if (usChannel.name.includes('CNBC')) pageUrl = 'https://www.cnbc.com/live-tv';

    if (!pageUrl) return;

    router.push({
      pathname: '/NetworkInspectorScreen',
      params: { channelId: String(channelId), pageUrl },
    });
  };

  // Refresh current stream URL whenever this screen regains focus (e.g. after Reload flow)
  useFocusEffect(
    React.useCallback(() => {
      let isActive = true;

      const refresh = async () => {
        if (region === 'usa') {
          const channel = NEWS_CHANNELS.usa.find((c) => c.streamIndex === currentStreamIndex);
          if (channel) {
            const cached = await getM3U8Link(String(channel.id));
            if (isActive && cached && cached.includes('.m3u8')) {
              setCurrentStreamUrl(cached);
              return;
            }
          }
        }
        // fallback to static if no cached or not usa
        if (isActive) {
          setCurrentStreamUrl(STREAMS[region][currentStreamIndex]);
        }
      };

      refresh();

      return () => {
        isActive = false;
      };
    }, [region, currentStreamIndex])
  );

  return (
    <SafeAreaProvider>
      <View style={styles.safeArea}>
        <View style={styles.container}>
          {/* VideoPlayerSection rendered outside of hidden content to maintain fullscreen visibility */}
          <VideoPlayerSection
            streamUrl={currentStreamUrl || STREAMS[region][currentStreamIndex]}
            isFullscreen={isFullscreen}
            setIsFullscreen={setIsFullscreen}
            fullscreenEmitter={fullscreenEmitter}
          />
          <View style={[styles.contentArea, isFullscreen && styles.hiddenContent]}>
            <View style={styles.regionSelector}>
              <View style={styles.regionToggle}>
                <Pressable
                  style={[styles.regionButton, region === 'india' && styles.activeRegion]}
                  onPress={() => setRegion('india')}>
                  <Text style={[styles.regionText, region === 'india' && styles.activeRegionText]}>
                    India
                  </Text>
                </Pressable>
                <Pressable
                  style={[styles.regionButton, region === 'usa' && styles.activeRegion]}
                  onPress={() => setRegion('usa')}>
                  <Text style={[styles.regionText, region === 'usa' && styles.activeRegionText]}>
                    USA
                  </Text>
                </Pressable>
              </View>
            </View>
            <NewsChannelList
              region={region}
              onChannelSelect={handleChannelSelect}
              onReloadChannel={handleReloadChannel}
            />
          </View>
        </View>
      </View>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#0A0E1A',
  },
  container: {
    flex: 1,
    backgroundColor: '#0A0E1A',
  },
  contentArea: {
    flex: 1,
        paddingBottom: 1,
  },
  hiddenContent: {
    display: 'none',
  },
  regionSelector: {
    paddingHorizontal: 20,
    // paddingVertical: ,
  },
  regionToggle: {
    flexDirection: 'row',
    backgroundColor: 'rgba(20, 24, 35, 0.7)',
    borderRadius: 12,
    padding: 4,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.08)',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 8,
  },
  regionButton: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 8,
  },
  activeRegion: {
    backgroundColor: '#3B82F6',
  },
  regionText: {
    color: '#FFFFFF',
    textAlign: 'center',
    fontWeight: '600',
    fontSize: 16,
  },
  activeRegionText: {
    color: '#FFFFFF',
  },
});
