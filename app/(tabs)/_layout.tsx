import { Tabs } from 'expo-router';
import { TabBarVisibilityProvider } from '@/components/TabBarVisibilityContext';
import GlassTabBar from '@/components/GlassTabBar';
import { useEffect, useState } from 'react';
import { Platform } from 'react-native';

// Create a custom event emitter for fullscreen changes
const createEventEmitter = () => {
  const listeners = new Set<(isFullscreen: boolean) => void>();
  return {
    emit: (isFullscreen: boolean) => {
      for (const listener of listeners) {
        listener(isFullscreen);
      }
    },
    subscribe: (listener: (isFullscreen: boolean) => void) => {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
  };
};

// Create a singleton event emitter
export const fullscreenEmitter = createEventEmitter();

export default function TabLayout() {
  const [isFullscreen, setIsFullscreen] = useState(false);

  useEffect(() => {
    // Only set up the listener if we're not on web platform
    if (Platform.OS !== 'web') {
      const unsubscribe = fullscreenEmitter.subscribe((newFullscreenState) => {
        setIsFullscreen(newFullscreenState);
      });

      return () => unsubscribe();
    }
    // For web platform, use the standard fullscreenchange event
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement);
    };

    document.addEventListener('fullscreenchange', handleFullscreenChange);
    return () => document.removeEventListener('fullscreenchange', handleFullscreenChange);
  }, []);

  return (
    <TabBarVisibilityProvider>
      <Tabs
        tabBar={isFullscreen ? () => null : (props) => <GlassTabBar {...props} />}
        screenOptions={{
          headerShown: true,
        }}
      >
        <Tabs.Screen
          name="index"
          options={{
            title: 'Home',
            headerShown: false,
          }}
        />
        <Tabs.Screen
          name="settings"
          options={{
            title: 'Settings',
            headerShown: false,
          }}
        />
      </Tabs>
    </TabBarVisibilityProvider>
  );
}