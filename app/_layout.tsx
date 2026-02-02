import { useEffect, useState } from 'react';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { Platform } from 'react-native';
import { useFrameworkReady } from '@/hooks/useFrameworkReady';
import { SplashScreen } from 'expo-router';
import { fullscreenEmitter } from './(tabs)/_layout';

// Prevent the splash screen from auto-hiding before asset loading is complete.
SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const [isFullscreen, setIsFullscreen] = useState(false);
  useFrameworkReady();

  useEffect(() => {
    SplashScreen.hideAsync();

    // Subscribe to fullscreen changes
    const unsubscribe = fullscreenEmitter.subscribe((newFullscreenState) => {
      setIsFullscreen(newFullscreenState);
    });

    return () => {
      unsubscribe();
    };
  }, []);

  return (
    <>
      <Stack screenOptions={{ headerShown: false }}>
        <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
      </Stack>
      <StatusBar style="light" hidden={isFullscreen} />
    </>
  );
}