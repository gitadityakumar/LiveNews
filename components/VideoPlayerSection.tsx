import { useVideoPlayer, VideoView } from 'expo-video';
import type { VideoPlayer } from 'expo-video';
import * as ScreenOrientation from 'expo-screen-orientation';
import { Maximize2, Minimize2, Pause, Play, Volume2, VolumeX } from 'lucide-react-native';
import { BlurView } from 'expo-blur';
import { useEffect, useRef, useState } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';
import Animated, { useAnimatedStyle, withTiming } from 'react-native-reanimated';

const AnimatedBlurView = Animated.createAnimatedComponent(BlurView);

interface VideoPlayerSectionProps {
  streamUrl: string;
  isFullscreen: boolean;
  setIsFullscreen: (value: boolean) => void;
  fullscreenEmitter: any;
}

export default function VideoPlayerSection({
  streamUrl,
  isFullscreen,
  setIsFullscreen,
  fullscreenEmitter,
}: VideoPlayerSectionProps) {
  const [showControls, setShowControls] = useState(true);
  const [isDelayed, setIsDelayed] = useState(false);
  const [isMounted, setIsMounted] = useState(true);
  const [isPlaying, setIsPlaying] = useState(true);
  const controlsTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const playerRef = useRef<VideoPlayer | null>(null);

  const player = useVideoPlayer(streamUrl, (player) => {
    player.loop = true;
    player.play();
    setIsPlaying(true);
  });

  // Store player reference
  // Sync local playing state with player state
  useEffect(() => {
    const interval = setInterval(() => {
      if (isMounted && playerRef.current) {
        setIsPlaying(playerRef.current.playing);
      }
    }, 100);

    return () => clearInterval(interval);
  }, [isMounted]);

  useEffect(() => {
    playerRef.current = player;
  }, [player]);

  // Cleanup only on unmount
  useEffect(() => {
    return () => {
      if (playerRef.current) {
        try {
          playerRef.current.pause();
        } catch (error) {
          // Player may already be released, ignore error
        }
      }
    };
  }, []);

  useEffect(() => {
    if (Platform.OS !== 'web') {
      ScreenOrientation.unlockAsync();
    }

    // Track component mount state
    setIsMounted(true);
    return () => {
      setIsMounted(false);
    };
  }, []);

  useEffect(() => {
    // Clear any existing timeout
    if (controlsTimeoutRef.current) {
      clearTimeout(controlsTimeoutRef.current);
    }

    // Set new timeout when controls are shown
    if (showControls && isMounted) {
      controlsTimeoutRef.current = setTimeout(() => {
        if (isMounted) {
          setShowControls(false);
        }
      }, 3000);
    }

    return () => {
      if (controlsTimeoutRef.current) {
        clearTimeout(controlsTimeoutRef.current);
      }
    };
  }, [showControls, isMounted]);

  const handleScreenTouch = () => {
    // Toggle controls visibility
    setShowControls(true);
  };

  const toggleFullscreen = async () => {
    const newFullscreenState = !isFullscreen;

    // Ensure player continues playing during orientation change
    if (player && isMounted) {
      try {
        if (!player.playing) {
          player.play();
        }
      } catch (error) {
        // Handle error silently
      }
    }

    if (Platform.OS === 'web') {
      const elem = document.documentElement;
      if (!document.fullscreenElement) {
        await elem.requestFullscreen();
      } else {
        await document.exitFullscreen();
      }
    } else {
      if (newFullscreenState) {
        await ScreenOrientation.lockAsync(
          ScreenOrientation.OrientationLock.LANDSCAPE
        );
      } else {
        await ScreenOrientation.lockAsync(
          ScreenOrientation.OrientationLock.PORTRAIT
        );
      }
    }

    setIsFullscreen(newFullscreenState);
    fullscreenEmitter.emit(newFullscreenState);
  };

  const jumpToLive = async () => {
    if (!isMounted) return;

    setIsDelayed(false);
    try {
      if (player.duration > 0) {
        player.currentTime = player.duration;
        player.play();
      } else {
        await player.replaceAsync(streamUrl);
      }
    } catch (error) {
      // Handle player error silently - player may be released
    }
  };

  useEffect(() => {
    const checkPlaybackStatus = setInterval(() => {
      if (isMounted && player.duration > 0) {
        const isDelayed = player.currentTime < player.duration - 5;
        setIsDelayed(isDelayed);
      }
    }, 1000);

    return () => clearInterval(checkPlaybackStatus);
  }, [player, isMounted]);

  // Ensure video continues playing when entering fullscreen mode
  useEffect(() => {
    if (isMounted && isFullscreen) {
      const timer = setTimeout(() => {
        try {
          if (!player.playing) {
            player.play();
          }
        } catch (error) {
          // Handle error silently - player may be released
        }
      }, 100); // Small delay to allow orientation change to complete

      return () => clearTimeout(timer);
    }
  }, [isFullscreen, isMounted, player]);

  const controlsOpacity = useAnimatedStyle(() => ({
    opacity: withTiming(showControls ? 1 : 0, {
      duration: 300,
    }),
  }), [showControls]);

  const containerStyle = isFullscreen ? styles.fullscreenContainer : styles.container;

  return (
    <View style={containerStyle}>
      <View style={isFullscreen ? styles.fullscreenVideoContainer : styles.videoContainer}>
        <VideoView
          key={streamUrl}
          player={player}
          style={isFullscreen ? styles.fullscreenVideo : styles.video}
          contentFit="contain"
          nativeControls={false}
          fullscreenOptions={{ enable: false }}
        />
        
        {/* Full-screen overlay for controls */}
        <View style={[styles.overlayContainer, { zIndex: isFullscreen ? 9999 : 10 }]}>
          {/* Touch overlay - Always visible to capture touches */}
          <Pressable
            onPress={handleScreenTouch}
            style={styles.overlayTouchArea}
            android_ripple={{ color: 'transparent' }}
          />

          {/* Centered Play/Pause Button */}
          <Animated.View 
            style={[styles.centerControls, controlsOpacity]}
            pointerEvents={showControls ? 'auto' : 'none'}
          >
            <Pressable
              onPress={() => {
                if (!isMounted) return;
                try {
                  if (player.playing) {
                    player.pause();
                    setIsPlaying(false);
                  } else {
                    player.play();
                    setIsPlaying(true);
                  }
                } catch (error) {
                  // Handle error silently - player may be released
                }
              }}
              style={styles.centerControlButton}
            >
              {isPlaying ? (
                <Pause color="white" size={32} />
              ) : (
                <Play color="white" size={32} />
              )}
            </Pressable>
          </Animated.View>

          {/* Bottom Left Controls - LIVE Badge and Mute/Unmute */}
          <Animated.View 
            style={[styles.bottomLeftControls, controlsOpacity]}
            pointerEvents={showControls ? 'auto' : 'none'}
          >
            <View style={styles.bottomLeftContainer}>
              <Pressable onPress={jumpToLive} style={styles.liveIndicator}>
                <View
                  style={[
                    styles.liveDot,
                    { backgroundColor: isDelayed ? '#fff' : '#E74C3C' },
                  ]}
                />
                <Text style={styles.liveText}>LIVE</Text>
              </Pressable>
              <Pressable
                onPress={() => {
                  if (!isMounted) return;
                  try {
                    player.muted = !player.muted;
                  } catch (error) {
                    // Handle error silently - player may be released
                  }
                }}
                style={styles.controlButton}
              >
                {player.muted ? (
                  <VolumeX color="white" size={20} />
                ) : (
                  <Volume2 color="white" size={20} />
                )}
              </Pressable>
            </View>
          </Animated.View>

          {/* Bottom Right Controls - Fullscreen Button */}
          <Animated.View 
            style={[styles.bottomRightControls, controlsOpacity]}
            pointerEvents={showControls ? 'auto' : 'none'}
          >
            <Pressable
              onPress={toggleFullscreen}
              style={styles.controlButton}
            >
              {isFullscreen ? (
                <Minimize2 color="white" size={24} />
              ) : (
                <Maximize2 color="white" size={24} />
              )}
            </Pressable>
          </Animated.View>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    height: 260,
    backgroundColor: '#0A0E1A',
    position: 'relative',
  },
  fullscreenContainer: {
    ...StyleSheet.absoluteFillObject,
    zIndex: 9999,
    backgroundColor: '#000',
  },
  // New style for fullscreen video container with SafeAreaView
  fullscreenVideoContainer: {
    flex: 1,
    backgroundColor: '#000',
    position: 'relative',
  },
  videoContainer: {
    flex: 1,
    backgroundColor: '#000',
    top:0,
    position: 'relative',
    justifyContent: 'center',
    alignItems: 'center',
  },
  video: {
    width: '100%',
    height: '100%',
    backgroundColor: '#000',
  },
  // New style for fullscreen video
  fullscreenVideo: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    width: '100%',
    height: '100%',
    backgroundColor: '#000',
  },
  // Full-screen overlay for touch handling
  fullScreenOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    zIndex: 1,
  },
  // Container for the overlay controls
  overlayContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    zIndex: 10,
  },
  // Touch area in the overlay (semi-transparent)
  overlayTouchArea: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.2)',
  },
  // Center controls for play/pause button
  centerControls: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1,
  },
  centerControlButton: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: 'rgba(42, 49, 66, 0.7)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  // Bottom left controls container (LIVE badge + Mute)
  bottomLeftControls: {
    position: 'absolute',
    bottom: 16,
    left: 16,
    zIndex: 20,
  },
  bottomLeftContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  // Bottom right controls container (Fullscreen button)
  bottomRightControls: {
    position: 'absolute',
    bottom: 16,
    right: 16,
    zIndex: 20,
  },
  // Control button style for small buttons
  controlButton: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: 'rgba(42, 49, 66, 0.7)',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
  },
  liveIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 100,
    borderWidth: 1,
    borderColor: '#E74C3C',
    backgroundColor: 'rgba(10, 14, 26, 0.8)',
  },
  liveDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#E74C3C',
    marginRight: 8,
  },
  liveText: {
    color: '#E74C3C',
    fontSize: 12,
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
});
