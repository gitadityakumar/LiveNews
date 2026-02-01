import { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import { View, Pressable, Text, StyleSheet, Platform } from 'react-native';
import { BlurView } from 'expo-blur';
import { LinearGradient } from 'expo-linear-gradient';
import { Home, Settings } from 'lucide-react-native';
import { useTabBarVisibility } from './TabBarVisibilityContext';
import Animated, { useAnimatedStyle, withTiming, withSpring } from 'react-native-reanimated';
import { useEffect } from 'react';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

export default function GlassTabBar({ state, descriptors, navigation }: BottomTabBarProps) {
  const { isTabBarVisible } = useTabBarVisibility();
  const insets = useSafeAreaInsets();
  const iconSize = 24;
  const iconColor = '#FFFFFF';

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [
        {
          translateY: withSpring(isTabBarVisible ? 0 : 150, {
            damping: 20,
            stiffness: 90,
          }),
        },
      ],
    };
  });

  const getIcon = (routeName: string, isFocused: boolean) => {
    const iconProps = {
      size: iconSize,
      color: isFocused ? '#3B82F6' : iconColor,
      strokeWidth: isFocused ? 2.5 : 2,
    };

    switch (routeName) {
      case 'index':
        return <Home {...iconProps} />;
      case 'settings':
        return <Settings {...iconProps} />;
      default:
        return null;
    }
  };

  return (
    <Animated.View style={[styles.container, animatedStyle]}>
      <LinearGradient
        colors={['rgba(10, 14, 26, 0.95)', 'rgba(10, 14, 26, 0.85)']}
        style={styles.gradient}
      >
        <BlurView intensity={30} tint="dark" style={styles.blurContainer}>
          <View style={[styles.tabBar, { paddingBottom: insets.bottom > 0 ? insets.bottom : 20 }]}>
            {state.routes.map((route, index) => {
              const { options } = descriptors[route.key];
              const label =
                options.tabBarLabel !== undefined
                  ? options.tabBarLabel
                  : options.title !== undefined
                  ? options.title
                  : route.name;

              const isFocused = state.index === index;

              const onPress = () => {
                const event = navigation.emit({
                  type: 'tabPress',
                  target: route.key,
                  canPreventDefault: true,
                });

                if (!isFocused && !event.defaultPrevented) {
                  navigation.navigate(route.name);
                }
              };

              return (
                <Pressable
                  key={route.key}
                  accessibilityRole="button"
                  accessibilityState={isFocused ? { selected: true } : {}}
                  onPress={onPress}
                  style={({ pressed }) => [
                    styles.tabItem,
                    pressed && styles.tabItemPressed,
                  ]}
                >
                  <View style={styles.iconContainer}>
                    {getIcon(route.name, isFocused)}
                  </View>
                  <Text style={[styles.tabLabel, isFocused && styles.tabLabelActive]}>
                    {typeof label === 'string' ? label : route.name}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        </BlurView>
      </LinearGradient>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    elevation: 0,
  },
  gradient: {
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
    overflow: 'hidden',
  },
  blurContainer: {
    overflow: 'hidden',
  },
  tabBar: {
    flexDirection: 'row',
    paddingTop: 12,
    paddingHorizontal: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderTopColor: 'rgba(255, 255, 255, 0.1)',
    // borderRadius:70, // Removing border radius to make it full width properly at bottom like standard tabs or keep design choice
  },
  tabItem: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 6,
    borderRadius: 50,
  },
  tabItemPressed: {
    backgroundColor: 'rgba(59, 130, 246, 0.1)',
  },
  iconContainer: {
    marginBottom: 3,
  },
  tabLabel: {
    fontSize: 12,
    fontWeight: '500',
    color: 'rgba(255, 255, 255, 0.6)',
  },
  tabLabelActive: {
    color: '#3B82F6',
  },
});
