import { View, StyleSheet, ScrollView, Text } from 'react-native';
import { useRef, useEffect, useState } from 'react';
import NewsChannelCard from './NewsChannelCard';
import { NEWS_CHANNELS } from '@/constants/data';
import { useTabBarVisibility } from './TabBarVisibilityContext';

interface NewsChannelListProps {
  region: 'india' | 'usa';
  onChannelSelect: (streamIndex: number) => void;
}

export default function NewsChannelList({ region, onChannelSelect }: NewsChannelListProps) {
  const channels = NEWS_CHANNELS[region];
  const scrollViewRef = useRef<ScrollView>(null);
  const [lastScrollY, setLastScrollY] = useState(0);
  const scrollTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const { setTabBarVisible } = useTabBarVisibility();

  const handleScroll = (event: any) => {
    const currentScrollY = event.nativeEvent.contentOffset.y;
    const isScrollingUp = currentScrollY < lastScrollY;
    const isScrollingDown = currentScrollY > lastScrollY && currentScrollY > 100;

    // Update tab bar visibility based on scroll direction
    if (isScrollingUp || isScrollingDown) {
      setTabBarVisible(isScrollingUp);
    }

    setLastScrollY(currentScrollY);

    // Clear existing timeout
    if (scrollTimeoutRef.current) {
      clearTimeout(scrollTimeoutRef.current);
    }

    // Show tab bar after scrolling stops
    scrollTimeoutRef.current = setTimeout(() => {
      setTabBarVisible(true);
    }, 150);
  };

  useEffect(() => {
    return () => {
      if (scrollTimeoutRef.current) {
        clearTimeout(scrollTimeoutRef.current);
      }
    };
  }, []);

  return (
    <View style={styles.container}>
      <ScrollView
        ref={scrollViewRef}
        style={styles.scrollView}
        showsVerticalScrollIndicator={false}
        onScroll={handleScroll}
        scrollEventThrottle={16}
      >
        {channels.map((channel) => (
          <NewsChannelCard
            key={channel.id}
            channel={channel}
            onPress={() => onChannelSelect(channel.streamIndex)}
          />
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 4,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: '#FFFFFF',
    paddingHorizontal: 20,
    marginBottom: 0,
    lineHeight: 5,
  },
  scrollView: {
    flex: 1,
  },
});
