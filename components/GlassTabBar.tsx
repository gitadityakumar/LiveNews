import { BlurView } from 'expo-blur';
import { Home, Settings } from 'lucide-react-native';
import { useRouter, usePathname } from 'expo-router';
import { Platform, Pressable, StyleSheet, View } from 'react-native';
import Animated, { useAnimatedStyle, withTiming } from 'react-native-reanimated';
import { useTabBarVisibility } from './TabBarVisibilityContext';

const AnimatedBlurView = Animated.createAnimatedComponent(BlurView);

interface TabBarButtonProps {
  icon: React.ReactNode;
  isActive: boolean;
  onPress: () => void;
}

function TabBarButton({ icon, isActive, onPress }: TabBarButtonProps) {
  return (
    <Pressable onPress={onPress} style={styles.tabButton}>
      <Animated.View
        style={[
          styles.iconContainer,
          isActive && styles.activeIconContainer,
        ]}
      >
        {icon}
      </Animated.View>
    </Pressable>
  );
}

export default function GlassTabBar() {
  const router = useRouter();
  const pathname = usePathname();
  const { isTabBarVisible } = useTabBarVisibility();

  const containerStyle = useAnimatedStyle(() => {
    return {
      transform: [
        {
          translateY: withTiming(isTabBarVisible ? 0 : 120, {
            duration: 300,
          }),
        },
      ],
      opacity: withTiming(isTabBarVisible ? 1 : 0, {
        duration: 300,
      }),
    };
  }, [isTabBarVisible]);

  const routes = [
    { name: 'index', icon: Home, label: 'Home' },
    { name: 'settings', icon: Settings, label: 'Settings' },
  ];

  return (
    <Animated.View style={[styles.container, containerStyle]}>
      <AnimatedBlurView
        intensity={Platform.OS === 'ios' ? 80 : 100}
        tint="dark"
        style={styles.blurView}
      >
        <View style={styles.tabBar}>
          {routes.map((route) => {
            const isActive = pathname === `/${route.name}` || (pathname === '/' && route.name === 'index');
            const Icon = route.icon;

            return (
              <TabBarButton
                key={route.name}
                icon={<Icon size={19} color={isActive ? '#3B82F6' : '#6B7280'} />}
                isActive={isActive}
                onPress={() => router.push(`/${route.name}`)}
              />
            );
          })}
        </View>
      </AnimatedBlurView>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    paddingBottom: 8,
  },
  blurView: {
    overflow: 'hidden',
  },
  tabBar: {
    flexDirection: 'row',
    backgroundColor: 'rgba(10, 14, 26, 0.3)',
    marginHorizontal: 16,
    marginBottom: 8,
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 8,
  },
  tabButton: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 8,
  },
  iconContainer: {
    width: 44,
    height: 44,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
  },
  activeIconContainer: {
    backgroundColor: 'rgba(59, 130, 246, 0.15)',
    borderWidth: 1,
    borderColor: 'rgba(59, 130, 246, 0.3)',
  },
});
