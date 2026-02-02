import { View, StyleSheet, NativeSyntheticEvent, NativeScrollEvent } from 'react-native';
import { useRef, useEffect, useState, useCallback } from 'react';
import DraggableFlatList, {
  ScaleDecorator,
  RenderItemParams,
} from 'react-native-draggable-flatlist';
import NewsChannelCard from './NewsChannelCard';
import { NEWS_CHANNELS } from '@/constants/data';
import { useTabBarVisibility } from './TabBarVisibilityContext';
import { getChannelOrder, saveChannelOrder } from '@/utils/channelOrderStorage';

interface NewsChannel {
  id: number;
  name: string;
  category: string;
  streamIndex: number;
}

interface NewsChannelListProps {
  region: 'india' | 'usa';
  onChannelSelect: (streamIndex: number) => void;
  onReloadChannel: (channelId: number) => void;
}

export default function NewsChannelList({ region, onChannelSelect, onReloadChannel }: NewsChannelListProps) {
  const [channels, setChannels] = useState<NewsChannel[]>([]);
  const { setTabBarVisible } = useTabBarVisibility();
  
  // Use refs for scroll tracking to avoid stale closures
  const lastScrollY = useRef(0);
  const scrollDirection = useRef<'up' | 'down' | 'none'>('none');

  // Load channels with saved order on mount or region change
  useEffect(() => {
    const loadChannels = async () => {
      const defaultChannels = NEWS_CHANNELS[region];
      const savedOrder = await getChannelOrder(region);

      if (savedOrder && savedOrder.length > 0) {
        const orderedChannels = savedOrder
          .map((id) => defaultChannels.find((c) => c.id === id))
          .filter(Boolean) as NewsChannel[];

        const newChannels = defaultChannels.filter(
          (c) => !savedOrder.includes(c.id)
        );

        setChannels([...orderedChannels, ...newChannels]);
      } else {
        setChannels(defaultChannels);
      }
    };

    loadChannels();
  }, [region]);

  const handleDragEnd = useCallback(
    ({ data }: { data: NewsChannel[] }) => {
      setChannels(data);
      const orderedIds = data.map((c) => c.id);
      saveChannelOrder(region, orderedIds);
    },
    [region]
  );

  // Use onScrollOffsetChange which DraggableFlatList properly exposes
  const handleScrollOffsetChange = useCallback(
    (offset: number) => {
      // Ignore negative scroll (bounce)
      if (offset < 0) return;

      const diff = offset - lastScrollY.current;

      // Scrolling down - hide tab bar
      if (diff > 5 && offset > 50) {
        if (scrollDirection.current !== 'down') {
          scrollDirection.current = 'down';
          setTabBarVisible(false);
        }
      }
      // Scrolling up - show tab bar
      else if (diff < -5) {
        if (scrollDirection.current !== 'up') {
          scrollDirection.current = 'up';
          setTabBarVisible(true);
        }
      }

      lastScrollY.current = offset;
    },
    [setTabBarVisible]
  );

  // Also handle regular onScroll as a fallback
  const handleScroll = useCallback(
    (event: NativeSyntheticEvent<NativeScrollEvent>) => {
      const offset = event.nativeEvent.contentOffset.y;
      handleScrollOffsetChange(offset);
    },
    [handleScrollOffsetChange]
  );

  const handleScrollBeginDrag = useCallback(
    (event: NativeSyntheticEvent<NativeScrollEvent>) => {
      lastScrollY.current = event.nativeEvent.contentOffset.y;
      scrollDirection.current = 'none';
    },
    []
  );

  const renderItem = useCallback(
    ({ item, drag, isActive }: RenderItemParams<NewsChannel>) => {
      return (
        <ScaleDecorator>
          <NewsChannelCard
            channel={item}
            onPress={() => onChannelSelect(item.streamIndex)}
            onReload={() => onReloadChannel(item.id)}
            onLongPress={drag}
            isActive={isActive}
          />
        </ScaleDecorator>
      );
    },
    [onChannelSelect, onReloadChannel]
  );

  return (
    <View style={styles.container}>
      <DraggableFlatList
        data={channels}
        keyExtractor={(item) => String(item.id)}
        renderItem={renderItem}
        onDragEnd={handleDragEnd}
        onScrollOffsetChange={handleScrollOffsetChange}
        onScroll={handleScroll}
        onScrollBeginDrag={handleScrollBeginDrag}
        scrollEventThrottle={16}
        showsVerticalScrollIndicator={false}
        activationDistance={10}
      />
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
});
