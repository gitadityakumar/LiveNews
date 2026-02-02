import AsyncStorage from '@react-native-async-storage/async-storage';

const CHANNEL_ORDER_KEY_PREFIX = 'channel_order_';

/**
 * Get the saved channel order for a region.
 * Returns an array of channel IDs in the user's preferred order, or null if not set.
 */
export async function getChannelOrder(region: 'india' | 'usa'): Promise<number[] | null> {
  try {
    const key = `${CHANNEL_ORDER_KEY_PREFIX}${region}`;
    const stored = await AsyncStorage.getItem(key);
    if (stored) {
      return JSON.parse(stored) as number[];
    }
    return null;
  } catch (error) {
    return null;
  }
}

/**
 * Save the channel order for a region.
 */
export async function saveChannelOrder(region: 'india' | 'usa', orderedIds: number[]): Promise<void> {
  try {
    const key = `${CHANNEL_ORDER_KEY_PREFIX}${region}`;
    await AsyncStorage.setItem(key, JSON.stringify(orderedIds));
  } catch (error) {
    // Silent fail
  }
}
