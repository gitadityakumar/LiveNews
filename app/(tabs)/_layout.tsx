import { Tabs } from 'expo-router';
import { Monitor, Settings } from 'lucide-react-native';
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
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          backgroundColor: '#1a1a1a',
          borderTopColor: '#333',
          display: isFullscreen ? 'none' : 'flex', // Hide the tab bar in fullscreen mode
        },
        tabBarActiveTintColor: '#ff3b30',
        tabBarInactiveTintColor: '#888',
      }}>
      <Tabs.Screen
        name="index"
        options={{
          title: 'Live News',
          tabBarIcon: ({ size, color }) => (
            <Monitor size={size} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="settings"
        options={{
          title: 'Settings',
          tabBarIcon: ({ size, color }) => (
            <Settings size={size} color={color} />
          ),
        }}
      />
    </Tabs>
  );
}