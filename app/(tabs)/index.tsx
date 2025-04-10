import { STREAMS } from '@/constants/data';
import { ResizeMode, Video } from 'expo-av';
import type { AVPlaybackStatus, AVPlaybackStatusSuccess } from 'expo-av';
import * as ScreenOrientation from 'expo-screen-orientation';
import { Maximize2, Minimize2, Pause, Play, SkipBack, SkipForward, Volume2, VolumeX } from 'lucide-react-native';
import { useEffect, useRef, useState } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';
import { fullscreenEmitter } from './_layout';

export default function LiveNews() {
  const [region, setRegion] = useState<'india' | 'usa'>('india');
  const [isPlaying, setIsPlaying] = useState(true);
  const [isMuted, setIsMuted] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [showControls, setShowControls] = useState(true);
  const [currentStreamIndex, setCurrentStreamIndex] = useState(0);
  const [videoRef, setVideoRef] = useState<Video | null>(null);
  const [isBuffering, setIsBuffering] = useState(false);
  const [isDelayed, setIsDelayed] = useState(false);
  const controlsTimeoutRef = useRef<NodeJS.Timeout>();

  useEffect(() => {
    if (Platform.OS !== 'web') {
      ScreenOrientation.unlockAsync();
    }
  }, []);

  useEffect(() => {
    if (showControls) {
      if (controlsTimeoutRef.current) {
        clearTimeout(controlsTimeoutRef.current);
      }
      controlsTimeoutRef.current = setTimeout(() => {
        setShowControls(false);
      }, 3000);
    }

    return () => {
      if (controlsTimeoutRef.current) {
        clearTimeout(controlsTimeoutRef.current);
      }
    };
  }, [showControls]);

  const handleScreenTouch = () => {
    setShowControls(true);
  };

  const toggleFullscreen = async () => {
    const newFullscreenState = !isFullscreen;

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
    fullscreenEmitter.emit(newFullscreenState); // Emit fullscreen state change
  };

  const switchStream = async (direction: 'next' | 'prev') => {
    const streams = STREAMS[region];
    let newIndex: number;

    if (direction === 'next') {
      newIndex = (currentStreamIndex + 1) % streams.length;
    } else {
      newIndex = currentStreamIndex === 0 ? streams.length - 1 : currentStreamIndex - 1;
    }

    setCurrentStreamIndex(newIndex);
    if (videoRef) {
      await videoRef.loadAsync({ uri: streams[newIndex] }, { shouldPlay: true });
    }
  };

  const handleBuffering = (status: AVPlaybackStatus) => {
    if (status.isLoaded) {
      const playbackStatus = status as AVPlaybackStatusSuccess;
      setIsBuffering(playbackStatus.isBuffering);

      // Adjust delay threshold dynamically based on playback position, ensuring durationMillis is defined
      if (playbackStatus.durationMillis !== undefined) {
        const isDelayed = playbackStatus.positionMillis < playbackStatus.durationMillis - 5000;
        setIsDelayed(isDelayed);
        setIsBuffering(isDelayed || playbackStatus.isBuffering);
      } else {
        setIsDelayed(false); // Default to not delayed if durationMillis is undefined
        setIsBuffering(playbackStatus.isBuffering);
      }
    }
  };

  const handleLivePress = async () => {
    if (videoRef) {
      const currentStream = STREAMS[region][currentStreamIndex];
      const status = await videoRef.getStatusAsync();

      if (status.isLoaded) {
        // Seek to the live edge if the stream is loaded
        await videoRef.setPositionAsync(status.durationMillis || 0);
        await videoRef.playAsync();
      } else {
        // Reload the stream if not loaded
        await videoRef.loadAsync({ uri: currentStream }, { shouldPlay: true });
      }
      setIsDelayed(false);
    }
  };

  const jumpToLive = async () => {
    setIsDelayed(false);
    if (videoRef) {
      const currentStream = STREAMS[region][currentStreamIndex];
      const status = await videoRef.getStatusAsync();

      if (status.isLoaded) {
        // Seek to the live edge if the stream is loaded
        await videoRef.setPositionAsync(status.durationMillis || 0);
        await videoRef.playAsync();
      } else {
        // Reload the stream if not loaded
        await videoRef.loadAsync({ uri: currentStream }, { shouldPlay: true });
      }
    }
  };

  return (
    <View style={styles.container}>
      <View style={[styles.header, isFullscreen && styles.hiddenHeader]}>
        <Text style={styles.title}>Live News</Text>
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

      <Pressable 
        style={[styles.videoContainer, isFullscreen && styles.fullscreenVideo]}
        onPress={handleScreenTouch}
      >
        <Video
          ref={(ref) => setVideoRef(ref)}
          style={styles.video}
          source={{ uri: STREAMS[region][currentStreamIndex] }}
          resizeMode={ResizeMode.CONTAIN}
          shouldPlay={isPlaying}
          isMuted={isMuted}
          useNativeControls={false}
          isLooping
          onPlaybackStatusUpdate={handleBuffering}
        />
        <Pressable onPress={handleLivePress} style={styles.liveIndicator}>
          <View
            style={[
              styles.liveDot,
              { backgroundColor: isDelayed ? '#fff' : '#ff3b30' },
            ]}
          />
          <Text style={styles.liveText} onPress={jumpToLive}>LIVE</Text>
        </Pressable>
        {showControls && (
          <View style={styles.controls}>
            <Pressable onPress={() => switchStream('prev')} style={styles.controlButton}>
              <SkipBack color="white" size={24} />
            </Pressable>
            <Pressable onPress={() => setIsPlaying(!isPlaying)} style={styles.controlButton}>
              {isPlaying ? (
                <Pause color="white" size={24} />
              ) : (
                <Play color="white" size={24} />
              )}
            </Pressable>
            <Pressable onPress={() => switchStream('next')} style={styles.controlButton}>
              <SkipForward color="white" size={24} />
            </Pressable>
            <Pressable onPress={() => setIsMuted(!isMuted)} style={styles.controlButton}>
              {isMuted ? (
                <VolumeX color="white" size={24} />
              ) : (
                <Volume2 color="white" size={24} />
              )}
            </Pressable>
            <Pressable onPress={toggleFullscreen} style={styles.controlButton}>
              {isFullscreen ? (
                <Minimize2 color="white" size={24} />
              ) : (
                <Maximize2 color="white" size={24} />
              )}
            </Pressable>
          </View>
        )}
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
    
  },
  header: {
    paddingTop: 60,
    paddingHorizontal: 20,
    paddingBottom: 20,
    backgroundColor: '#1a1a1a',
  },
  hiddenHeader: {
    display: 'none',
  },
  title: {
    fontSize: 28,
    fontFamily: 'Inter-Bold',
    color: '#fff',
    marginBottom: 20,
  },
  regionToggle: {
    flexDirection: 'row',
    backgroundColor: '#333',
    borderRadius: 8,
    padding: 4,
  },
  regionButton: {
    flex: 1,
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 6,
  },
  activeRegion: {
    backgroundColor: '#ff3b30',
  },
  regionText: {
    color: '#fff',
    textAlign: 'center',
    fontFamily: 'Inter-SemiBold',
    fontSize: 16,
  },
  activeRegionText: {
    color: '#fff',
  },
  videoContainer: {
    flex: 1,
    backgroundColor: '#000',
    position: 'relative',
    justifyContent: 'center', // Center vertically
    alignItems: 'center', // Center horizontally
  },
  fullscreenVideo: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    zIndex: 999,
  },
  video: {
    width: '100%', // Ensure the video takes up the full width of the container
    height: '100%', // Ensure the video takes up the full height of the container
    backgroundColor: '#000',
  },
  liveIndicator: {
    position: 'absolute',
    top: 20,
    right: 20,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.6)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
  },
  fullscreenLiveIndicator: {
    top: 40,
    right: 40,
  },
  liveDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#ff3b30',
    marginRight: 6,
  },
  liveText: {
    color: '#fff',
    fontSize: 12,
    fontFamily: 'Inter-SemiBold',
  },
  controls: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.6)',
  },
  controlButton: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
    marginHorizontal: 10,
  },
  bufferingText: {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: [{ translateX: -50 }, { translateY: -50 }],
    color: '#fff',
    fontSize: 16,
    fontFamily: 'Inter-Regular',
  },
});