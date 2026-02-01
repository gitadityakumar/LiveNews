import {
  StyleSheet,
  View,
  Text,
  FlatList,
  Dimensions,
  TouchableOpacity,
  StatusBar,
} from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';
import { useVideoPlayer, VideoView } from 'expo-video';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  interpolate,
  Extrapolation,
  runOnJS,
  withTiming,
} from 'react-native-reanimated';
import {
  Gesture,
  GestureDetector,
  GestureHandlerRootView,
} from 'react-native-gesture-handler';
import { X, RefreshCw } from 'lucide-react-native';

const { width: SCREEN_WIDTH, height: SCREEN_HEIGHT } = Dimensions.get('window');

// --- Configuration Constants ---
const VIDEO_HEIGHT_EXPANDED = 250;
const VIDEO_WIDTH_EXPANDED = SCREEN_WIDTH;
const VIDEO_HEIGHT_MINIMIZED = 120;
const VIDEO_WIDTH_MINIMIZED = 160;
const MARGIN_BOTTOM = 0; // We handle safety manually
const ANIMATION_CONFIG = { duration: 200 };



// --- Video Source ---
const VIDEO_SOURCE =
  'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4';

// Dummy Data
const NEWS_DATA = Array.from({ length: 20 }).map((_, i) => ({
  id: String(i),
  title: i % 2 === 0 ? "ET Swadesh" : "Zee Business",
  subtitle: i % 2 === 0 ? "English News" : "International",
  circleText: i % 2 === 0 ? "ES" : "ZB",
  circleColor: i % 2 === 0 ? "#1e293b" : "#334155",
}));

export default function DraggableVideoDemo() {
  return (
    <GestureHandlerRootView style={styles.container}>
      <StatusBar barStyle="light-content" />
      <View style={styles.contentContainer}>
         <NewsList />
      </View>
      <DraggableVideoPlayer />
    </GestureHandlerRootView>
  );
}

function DraggableVideoPlayer() {
  // --- Shared Values ---
  // progress: 0.0 (Expanded) -> 1.0 (Minimized)
  const progress = useSharedValue(0); 
  
  // Position Translations (Used for Free Drag when minimized)
  const translateX = useSharedValue(0);
  const translateY = useSharedValue(0);
  
  // Context to store start values during gestures
  const ctx = useSharedValue({ startP: 0, startX: 0, startY: 0 });

  const insets = useSafeAreaInsets();

  const player = useVideoPlayer(VIDEO_SOURCE, (player) => {
    player.loop = true;
    player.play();
  });

  // Calculate Corners for Snapping
  // Default Dock Position: Bottom Right
  const SAFE_MARGIN = 16;
  const BOTTOM_TAB_OFFSET = 80; // approximate tab bar height
  
  const MAX_Y = SCREEN_HEIGHT - VIDEO_HEIGHT_MINIMIZED - BOTTOM_TAB_OFFSET - SAFE_MARGIN;
  const MIN_Y = insets.top + SAFE_MARGIN; // Top safe area
  const MAX_X = SCREEN_WIDTH - VIDEO_WIDTH_MINIMIZED - SAFE_MARGIN;
  const MIN_X = SAFE_MARGIN;

  // Defines the "Resting" translation when minimized (Bottom Right)
  // When progress goes 0->1, we interpolate to this relative offset effectively?
  // Strategy:
  // When 'Expanded': X=0, Y=insets.top
  // When 'Minimized': we use translateX/Y. 
  
  // To keep it continuous:
  // We will drive EVERYTHING via translateX / translateY and Width / Height.
  // 'progress' mainly controls the SIZE.
  
  const containerStyle = useAnimatedStyle(() => {
    // 1. Calculate Size based on Progress
    const height = interpolate(
      progress.value,
      [0, 1],
      [VIDEO_HEIGHT_EXPANDED, VIDEO_HEIGHT_MINIMIZED],
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
        currentY = interpolate(progress.value, [0, 1], [insets.top, MAX_Y]);
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
            // Snap Logic vs Expand Logic
            // If dragging UP significantly? Expand.
            // Else? Keep in bounds.
            
            // Check for Expand Gesture
            if (event.translationY < -50 && event.velocityY < -500) {
                 // Expand!
                 progress.value = withTiming(0, ANIMATION_CONFIG);
            } else {
                // Stay Mini - Snap to Nearest Side?
                // User said "Draggable Anywhere", but usually keeping it in bounds is good.
                const clampedX = Math.max(MIN_X, Math.min(MAX_X, translateX.value));
                const clampedY = Math.max(MIN_Y, Math.min(MAX_Y, translateY.value));
                
                translateX.value = withTiming(clampedX, ANIMATION_CONFIG);
                translateY.value = withTiming(clampedY, ANIMATION_CONFIG);
            }
        } else {
            // Transition Snap
             if (progress.value > 0.3 || (event.velocityY > 500)) {
                 // Go to Mini
                 progress.value = withTiming(1, ANIMATION_CONFIG);
                 
                 // Also set final X/Y
                 translateX.value = withTiming(MAX_X, ANIMATION_CONFIG);
                 translateY.value = withTiming(MAX_Y, ANIMATION_CONFIG);
             } else {
                 // Go Back to Full
                 progress.value = withTiming(0, ANIMATION_CONFIG);
             }
        }
    });

  const tapGesture = Gesture.Tap().onEnd(() => {
      if (progress.value === 1) {
          // Expand
          progress.value = withTiming(0, ANIMATION_CONFIG);
      }
  });

  // Combine gestures
  // Use 'Race' or 'Simultaneous'? 
  // If we are Mini, we want tap or drag.
  const composed = Gesture.Race(panGesture, tapGesture);

  // Close Button Opacity
  const closeStyle = useAnimatedStyle(() => ({
      opacity: progress.value < 0.8 ? 0 : (progress.value - 0.8) * 5, // Fade in at end
      transform: [{ scale: progress.value }]
  }));

  return (
    <GestureDetector gesture={composed}>
      <Animated.View style={[styles.videoContainer, containerStyle]}>
         {/* Close Button Overlay */}
         <Animated.View style={[styles.closeBtn, closeStyle]}>
            <TouchableOpacity onPress={() => console.log("Close")}>
                <View style={styles.closeIconBg}>
                  <X size={14} color="white" />
                </View>
            </TouchableOpacity>
         </Animated.View>
      
         <VideoView
          style={styles.video}
          player={player}
          nativeControls={false}
          contentFit="cover"
          allowsPictureInPicture
        />
      </Animated.View>
    </GestureDetector>
  );
}

function NewsList() {
    const renderItem = ({ item }: { item: typeof NEWS_DATA[0] }) => (
      <View style={styles.newsItem}>
        <View style={[styles.avatarCircle, { backgroundColor: item.circleColor }]}>
          <Text style={styles.avatarText}>{item.circleText}</Text>
        </View>
        <View style={styles.newsTextContainer}>
          <Text style={styles.newsTitle}>{item.title}</Text>
          <Text style={styles.newsSubtitle}>{item.subtitle}</Text>
        </View>
        <TouchableOpacity style={styles.refreshButton}>
           <RefreshCw size={20} color="#94a3b8" />
        </TouchableOpacity>
      </View>
    );

    return (
        <View style={{flex: 1}}>
          <SafeAreaView style={styles.safeArea}>
             <View style={styles.headerTabs}>
                  <View style={styles.activeTab}><Text style={styles.activeTabText}>India</Text></View>
                  <View style={styles.inactiveTab}><Text style={styles.inactiveTabText}>USA</Text></View>
             </View>
          </SafeAreaView>
          <FlatList
            data={NEWS_DATA}
            renderItem={renderItem}
            keyExtractor={item => item.id}
            contentContainerStyle={{ paddingTop: VIDEO_HEIGHT_EXPANDED, paddingBottom: 100 }}
            showsVerticalScrollIndicator={false}
          />
        </View>
    );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0f172a',
  },
  contentContainer: {
    flex: 1,
    zIndex: 0,
  },
  videoContainer: {
    position: 'absolute',
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
  // Item Styles
  newsItem: {
      flexDirection: 'row',
      backgroundColor: '#1e293b',
      marginHorizontal: 16,
      marginBottom: 12,
      padding: 16,
      borderRadius: 16,
      alignItems: 'center',
      borderWidth: 1,
      borderColor: '#334155',
  },
  avatarCircle: { width: 48, height: 48, borderRadius: 12, justifyContent: 'center', alignItems: 'center', marginRight: 16 },
  avatarText: { color: 'white', fontWeight: 'bold', fontSize: 16 },
  newsTextContainer: { flex: 1 },
  newsTitle: { color: '#f8fafc', fontWeight: 'bold', fontSize: 16, marginBottom: 2 },
  newsSubtitle: { color: '#94a3b8', fontSize: 14 },
  refreshButton: { padding: 4 },
  safeArea: { backgroundColor: '#0f172a' },
  headerTabs: { flexDirection: 'row', padding: 16 },
  activeTab: { backgroundColor: '#3b82f6', paddingVertical: 8, paddingHorizontal: 20, borderRadius: 8, marginRight: 12 },
  inactiveTab: { paddingVertical: 8, paddingHorizontal: 20 },
  activeTabText: { color: 'white', fontWeight: 'bold' },
  inactiveTabText: { color: '#94a3b8' },
});
