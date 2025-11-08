import AsyncStorage from '@react-native-async-storage/async-storage';

const M3U8_STORAGE_PREFIX = '@m3u8_';

// Build the AsyncStorage key for a channel
export const buildM3U8Key = (channelId: string): string => `${M3U8_STORAGE_PREFIX}${channelId}`;

// Persist a found .m3u8 link for a channel
export const saveM3U8Link = async (channelId: string, url: string): Promise<void> => {
  if (!channelId || !url || !url.includes('.m3u8')) return;
  try {
    const key = buildM3U8Key(channelId);
    await AsyncStorage.setItem(key, url);
  } catch (error) {
    console.warn('Failed to save m3u8 URL', { channelId, error });
  }
};

// Retrieve latest .m3u8 link for a channel
export const getM3U8Link = async (channelId: string): Promise<string | null> => {
  try {
    const key = buildM3U8Key(channelId);
    const value = await AsyncStorage.getItem(key);
    return value || null;
  } catch (error) {
    console.warn('Failed to load m3u8 URL', { channelId, error });
    return null;
  }
};

// Optional helper to clear link (for debugging/reset)
export const clearM3U8Link = async (channelId: string): Promise<void> => {
  try {
    const key = buildM3U8Key(channelId);
    await AsyncStorage.removeItem(key);
  } catch (error) {
    console.warn('Failed to clear m3u8 URL', { channelId, error });
  }
};

export default {
  saveM3U8Link,
  getM3U8Link,
  clearM3U8Link,
};