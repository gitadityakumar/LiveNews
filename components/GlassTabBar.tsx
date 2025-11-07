import { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import { View, Pressable, Text, StyleSheet } from 'react-native';
import { BlurView } from 'expo-blur';
import { LinearGradient } from 'expo-linear-gradient';
import { Home, Settings } from 'lucide-react-native';
import { useTabBarVisibility } from './TabBarVisibilityContext';

export default function GlassTabBar({ state, descriptors, navigation }: BottomTabBarProps) {
  const { isTabBarVisible } = useTabBarVisibility();
  const iconSize = 24;
  const iconColor = '#FFFFFF';

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

  if (!isTabBarVisible) {
    return null;
  }

  return (
    <View style={styles.container}>
      <LinearGradient
        colors={['rgba(10, 14, 26, 0.95)', 'rgba(10, 14, 26, 0.85)']}
        style={styles.gradient}
      >
        <BlurView intensity={30} tint="dark" style={styles.blurContainer}>
          <View style={styles.tabBar}>
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
    </View>
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
    paddingTop: 8,
    paddingBottom: 6,
    paddingHorizontal: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderTopColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius:70,
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
