import React from 'react';

import {
  StyleSheet,
  View,
  Dimensions,
  TouchableOpacity,
  StatusBar,
  Text,
  ActivityIndicator,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useVideoPlayer, VideoView, VideoPlayerStatus } from 'expo-video';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  interpolate,
  Extrapolation,
  withTiming,
  SharedValue,
  runOnJS,
} from 'react-native-reanimated';
import {
  Gesture,
  GestureDetector,
  GestureHandlerRootView,
} from 'react-native-gesture-handler';
import { X, Maximize2, Minimize2, Pause, Play, Volume2, VolumeX, MoveDiagonal, Scaling, ArrowUpLeft, ArrowDownRight } from 'lucide-react-native';
import { useState, useRef, useEffect } from 'react';
import * as ScreenOrientation from 'expo-screen-orientation';
import { fullscreenEmitter } from '../app/(tabs)/_layout'; // Import emitter if needed or pass as prop
import { BlurView } from 'expo-blur';

const { width: SCREEN_WIDTH, height: SCREEN_HEIGHT } = Dimensions.get('window');

// --- Configuration Constants ---
// export const VIDEO_HEIGHT_EXPANDED = 260; // REMOVED: Calculated dynamically
// export const VIDEO_WIDTH_EXPANDED = SCREEN_WIDTH; // REMOVED: Calculated dynamically
export const VIDEO_WIDTH_MINIMIZED = 180;
export const VIDEO_HEIGHT_MINIMIZED = VIDEO_WIDTH_MINIMIZED * (9/16); // 16:9 Ratio
const MARGIN_BOTTOM = 0; 
const ANIMATION_CONFIG = { duration: 200 };

import { useWindowDimensions } from 'react-native';

interface DraggableVideoPlayerProps {
  streamUrl: string;
  progress: SharedValue<number>;
  style?: any;
  onFullscreenChange?: (isFullscreen: boolean) => void;
}

export default function DraggableVideoPlayer({ streamUrl, progress, style, onFullscreenChange }: DraggableVideoPlayerProps) {
  // --- Shared Values ---
  // progress: 0.0 (Expanded) -> 1.0 (Minimized)
  
  // progress: 0.0 (Expanded) -> 1.0 (Minimized)
  
  // progress: 0.0 (Expanded) -> 1.0 (Minimized)
  
  const windowDims = useWindowDimensions();
  const screenDims = Dimensions.get('screen'); // Use screen dims for full coverage
  
  // Robustly determine orientation and dimensions
  const isLandscape = windowDims.width > windowDims.height;
  
  // In landscape, we want the FULL screen width (including notch area if possible)
  // We use the larger usage of screen vs window to ensure we cover background
  const VIDEO_WIDTH_EXPANDED = isLandscape ? Math.max(screenDims.width, screenDims.height) : windowDims.width;
  
  // Calculate 16:9 height based on the expanded width
  const VIDEO_HEIGHT_EXPANDED = VIDEO_WIDTH_EXPANDED * (9/16); 

  // Dynamic Height: In landscape, use full screen height. In portrait, use 16:9.
  const VIDEO_HEIGHT_EXPANDED_DYNAMIC = isLandscape ? Math.min(screenDims.width, screenDims.height) : VIDEO_HEIGHT_EXPANDED;

  // Controls State
  
  // Controls State
  const [showControls, setShowControls] = useState(true);
  const [isPlaying, setIsPlaying] = useState(true);
  const [isDelayed, setIsDelayed] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [contentFit, setContentFit] = useState<'contain' | 'cover'>('contain');
  const [isLoading, setIsLoading] = useState(true);
  const controlsTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  
  // Position Translations (Used for Free Drag when minimized)
  const translateX = useSharedValue(0);
  const translateY = useSharedValue(0);
  
  // Context to store start values during gestures
  const ctx = useSharedValue({ startP: 0, startX: 0, startY: 0 });

  const insets = useSafeAreaInsets();

  // Create player for the stream
  // Create player for the stream
  const player = useVideoPlayer(streamUrl, (player) => {
    player.loop = true;
    player.play();
    setIsPlaying(true);
  });
  
  // Clean up player when component unmounts explicitly
  useEffect(() => {
    return () => {
        try {
            if (player) player.pause();
        } catch (e) {
            // Ignore error if player is already released
        }
    };
  }, [player]);

  // Track player status for loading indicator
  useEffect(() => {
    if (!player) return;
    
    const handleStatusChange = (payload: { status: VideoPlayerStatus }) => {
      setIsLoading(payload.status === 'loading' || payload.status === 'idle');
    };
    
    // Check initial status
    setIsLoading(player.status === 'loading' || player.status === 'idle');
    
    player.addListener('statusChange', handleStatusChange);
    return () => {
      player.removeListener('statusChange', handleStatusChange);
    };
  }, [player]);

  // Controls Logic
  useEffect(() => {
    if (showControls) {
      if (controlsTimeoutRef.current) clearTimeout(controlsTimeoutRef.current);
      controlsTimeoutRef.current = setTimeout(() => {
        setShowControls(false);
      }, 3000);
    }
    return () => {
      if (controlsTimeoutRef.current) clearTimeout(controlsTimeoutRef.current);
    };
  }, [showControls]);

  const togglePlayback = () => {
    if (player.playing) {
      player.pause();
      setIsPlaying(false);
    } else {
      player.play();
      setIsPlaying(true);
    }
  };

  const toggleMute = () => {
    player.muted = !player.muted;
  };

  const jumpToLive = async () => {
    setIsDelayed(false);
    if (player.duration > 0) {
      player.currentTime = player.duration;
      player.play();
    } else {
      player.replace(streamUrl); // Re-load stream
    }
  };
  
  const toggleFullscreen = async () => {
    // Basic orientation lock for "Fullscreen" feel
    const newFs = !isFullscreen;
    setIsFullscreen(newFs);
    if (onFullscreenChange) {
      runOnJS(onFullscreenChange)(newFs);
    }
    
    if (newFs) {
        await ScreenOrientation.lockAsync(ScreenOrientation.OrientationLock.LANDSCAPE);
    } else {
        await ScreenOrientation.lockAsync(ScreenOrientation.OrientationLock.PORTRAIT);
        setContentFit('contain'); // Reset Zoom when exiting fullscreen
    }
  };

  const toggleZoom = () => {
    setContentFit(prev => prev === 'contain' ? 'cover' : 'contain');
  };

  // Live Check Loop
  useEffect(() => {
    const interval = setInterval(() => {
      if (player.duration > 0) {
         // Simple check: if we are > 5s behind duration, we are delayed
         setIsDelayed(player.currentTime < player.duration - 5);
      }
    }, 1000);
    return () => clearInterval(interval);
  }, [player]);

  // Calculate Corners for Snapping
  const SAFE_MARGIN = 16;
  const BOTTOM_TAB_OFFSET = 80;
  
  const MAX_Y = windowDims.height - VIDEO_HEIGHT_MINIMIZED - BOTTOM_TAB_OFFSET - SAFE_MARGIN;
  const MIN_Y = insets.top + SAFE_MARGIN; 
  const MAX_X = windowDims.width - VIDEO_WIDTH_MINIMIZED - SAFE_MARGIN;
  const MIN_X = SAFE_MARGIN;

  const containerStyle = useAnimatedStyle(() => {
    // 1. Calculate Size based on Progress
    const height = interpolate(
      progress.value,
      [0, 1],
      [VIDEO_HEIGHT_EXPANDED_DYNAMIC, VIDEO_HEIGHT_MINIMIZED],
      Extrapolation.CLAMP
    );
    
    // Smooth width interpolation
    const width = interpolate(
        progress.value,
        [0, 1],
        [VIDEO_WIDTH_EXPANDED, VIDEO_WIDTH_MINIMIZED],
        Extrapolation.CLAMP
    );

    let currentX = 0;
    let currentY = 0;
    
    if (progress.value < 1) {
        // Transition Mode: Interpolate to Bottom Right
        // FIX: Start at 0, not insets.top, because we are already in a SafeAreaView or want flush positioning
        currentY = interpolate(progress.value, [0, 1], [0, MAX_Y]);
        currentX = interpolate(progress.value, [0, 1], [0, MAX_X]);
    } else {
        // Free Mode: Use Shared Values
        currentX = translateX.value;
        currentY = translateY.value;
    }

    return {
       width,
       height,
       transform: [
           { translateX: currentX },
           { translateY: currentY }
       ],
       // Corner Radius
       borderRadius: interpolate(progress.value, [0, 1], [0, 12]),
    };
  });
  
  const panGesture = Gesture.Pan()
    .onStart(() => {
        ctx.value = { 
            startP: progress.value, 
            startX: translateX.value, 
            startY: translateY.value 
        };
    })
    .onUpdate((event) => {
       const isMini = ctx.value.startP === 1;
       
       if (isMini) {
           // Free Drag Mode
           translateX.value = ctx.value.startX + event.translationX;
           translateY.value = ctx.value.startY + event.translationY;
       } else {
           // If Fullscreen, don't drag-minimize
           if (isFullscreen) return;

           // Transition Mode (Drag Y to minimize)
           const dragDist = MAX_Y; // Total Y distance
           
           // If we dragging DOWN, we increase progress
           // If dragging UP, we decrease
           const deltaP = event.translationY / dragDist;
           const newP = ctx.value.startP + deltaP;
           
           progress.value = Math.max(0, Math.min(1, newP));
           
           // Sync Free Drag values so they are ready when we hit 1.0!
           // This prevents jump when switching modes
           translateX.value = interpolate(progress.value, [0, 1], [0, MAX_X]);
           translateY.value = interpolate(progress.value, [0, 1], [0, MAX_Y]);
       }
    })
    .onEnd((event) => {
        const isMini = ctx.value.startP === 1;
        
        if (isMini) {
            // Check for Expand Gesture
            if (event.translationY < -50 && event.velocityY < -500) {
                 // Expand!
                 progress.value = withTiming(0, ANIMATION_CONFIG);
            } else {
                // Stay Mini - Snap to bounds
                const clampedX = Math.max(MIN_X, Math.min(MAX_X, translateX.value));
                const clampedY = Math.max(MIN_Y, Math.min(MAX_Y, translateY.value));
                
                translateX.value = withTiming(clampedX, ANIMATION_CONFIG);
                translateY.value = withTiming(clampedY, ANIMATION_CONFIG);
            }
        } else {
            // Check for Swipe Logic (Fullscreen toggle)
             // Swipe UP on expanded player (Portrait) -> Fullscreen
             if (progress.value === 0 && event.translationY < -50 && !isFullscreen) {
                  runOnJS(toggleFullscreen)();
                  return;
             }
             // Swipe DOWN on Fullscreen player (Landscape) -> Exit Fullscreen (Portrait)
             if (isFullscreen && event.translationY > 50) {
                  progress.value = withTiming(0, ANIMATION_CONFIG); // Ensure we stay expanded
                  runOnJS(toggleFullscreen)();
                  return;
             }

             // Standard Minimize Logic (Only in Portrait)
             if (!isFullscreen) {
                // Drag Down -> Minimize
                if (progress.value > 0.3 || (event.velocityY > 500)) {
                    // Go to Mini
                    progress.value = withTiming(1, ANIMATION_CONFIG);
                    
                    translateX.value = withTiming(MAX_X, ANIMATION_CONFIG);
                    translateY.value = withTiming(MAX_Y, ANIMATION_CONFIG);
                } else {
                    // Go Back to Full (Portrait)
                    progress.value = withTiming(0, ANIMATION_CONFIG);
                }
             }
        }
    });

  const tapGesture = Gesture.Tap().onEnd(() => {
    // If it's a tap on the miniplayer, expand it
      if (progress.value === 1) {
          progress.value = withTiming(0, ANIMATION_CONFIG);
      } else {
         // If expanded, toggle controls
         runOnJS(setShowControls)(!showControls);
      }
  });

  const pinchGesture = Gesture.Pinch()
    .onUpdate((event) => {
        if (!isFullscreen) return;
        
        // Simple threshold logic for Zoom Interaction
        if (event.scale > 1.2 && contentFit !== 'cover') {
            runOnJS(setContentFit)('cover');
        } else if (event.scale < 0.8 && contentFit !== 'contain') {
             runOnJS(setContentFit)('contain');
        }
    });

  const composed = Gesture.Race(panGesture, tapGesture, pinchGesture);

  // Close Button Opacity - Only show when minimized
  const closeStyle = useAnimatedStyle(() => ({
      opacity: progress.value < 0.8 ? 0 : (progress.value - 0.8) * 5, 
      transform: [{ scale: progress.value }]
  }));
  
  // Controls Opacity - Only show when expanded AND showControls is true
  const controlsStyle = useAnimatedStyle(() => {
      // If we are minimizing (progress > 0), hide controls immediately
      const opacity = (progress.value === 0 && showControls) ? withTiming(1) : withTiming(0);
      return { opacity, zIndex: progress.value === 0 && showControls ? 100 : -1 }; 
  });

  return (
    <GestureDetector gesture={composed}>
      <Animated.View style={[styles.videoContainer, containerStyle]}>
         {/* Close Button Overlay */}
         <Animated.View style={[styles.closeBtn, closeStyle]}>
            <TouchableOpacity onPress={() => {
                // TODO: Implement close logic
            }}>
                <View style={styles.closeIconBg}>
                  <X size={14} color="white" />
                </View>
            </TouchableOpacity>
         </Animated.View>
      
         <VideoView
          key={streamUrl} 
          style={[styles.video, style]} // Apply custom style if provided
          player={player}
          nativeControls={false}
          contentFit={contentFit}
          allowsPictureInPicture
        />
        
        {/* Loading Indicator */}
        {isLoading && (
          <BlurView intensity={40} tint="dark" style={styles.loadingOverlay}>
            <ActivityIndicator size="large" color="#3B82F6" />
          </BlurView>
        )}
        
        {/* Expanded Controls Overlay */}
        <Animated.View style={[styles.controlsOverlay, controlsStyle]} pointerEvents="box-none">
             {/* Center Play/Pause */}
             <View style={styles.centerControls}>
                <TouchableOpacity onPress={togglePlayback} style={styles.centerControlButton}>
                     {isPlaying ? <Pause color="white" size={32} /> : <Play color="white" size={32} />}
                </TouchableOpacity>
             </View>

             {/* Bottom Left: Live badge & Mute */}
             <View style={styles.bottomLeftControls}>
                  <TouchableOpacity onPress={jumpToLive} style={styles.liveIndicator}>
                      <View style={[styles.liveDot, { backgroundColor: isDelayed ? '#fff' : '#E74C3C' }]} />
                      <Text style={styles.liveText}>LIVE</Text>
                  </TouchableOpacity>

                  <TouchableOpacity onPress={toggleMute} style={styles.controlButton}>
                      {player.muted ? <VolumeX color="white" size={20} /> : <Volume2 color="white" size={20} />}
                  </TouchableOpacity>
             </View>
                          {/* Bottom Right: Fullscreen/Minimize OR Zoom Toggle */}
              <View style={styles.bottomRightControls}>
                  {isFullscreen ? (
                    <TouchableOpacity onPress={toggleZoom} style={styles.controlButton}>
                        {contentFit === 'cover' ? <Minimize2 color="white" size={20} /> : <Maximize2 color="white" size={20} />}
                    </TouchableOpacity>
                  ) : (
                    <TouchableOpacity onPress={toggleFullscreen} style={styles.controlButton}>
                        <Maximize2 color="white" size={20} />
                    </TouchableOpacity>
                  )}
              </View>
        </Animated.View>
      </Animated.View>
    </GestureDetector>
  );
}

const styles = StyleSheet.create({
  videoContainer: {
    position: 'absolute', // Absolute positioning is key
    top: 0,
    left: 0,
    backgroundColor: 'black',
    zIndex: 9999,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 5,
    elevation: 10,
  },
  video: {
    width: '100%',
    height: '100%',
    backgroundColor: '#000',
  },
  loadingOverlay: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    zIndex: 50,
  },
  closeBtn: {
      position: 'absolute',
      top: 5,
      left: 5,
      zIndex: 1000,
  },
  closeIconBg: {
      width: 24,
      height: 24,
      borderRadius: 12,
      backgroundColor: 'rgba(0,0,0,0.6)',
      justifyContent: 'center',
      alignItems: 'center',
  },
  // --- Controls Styles ---
  controlsOverlay: {
      ...StyleSheet.absoluteFillObject,
      justifyContent: 'space-between',
      padding: 16,
  },
  centerControls: {
      flex: 1,
      justifyContent: 'center',
      alignItems: 'center',
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
  bottomLeftControls: {
      position: 'absolute',
      bottom: 16,
      left: 16,
      flexDirection: 'row',
      alignItems: 'center',
      gap: 12,
  },
  bottomRightControls: {
      position: 'absolute',
      bottom: 16,
      right: 16,
  },
  controlButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
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
