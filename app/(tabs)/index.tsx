import { useState, useEffect } from 'react';
import { StyleSheet, View, Pressable, Text } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import VideoPlayerSection from '@/components/VideoPlayerSection';
import NewsChannelList from '@/components/NewsChannelList';
import { STREAMS } from '@/constants/data';
import { fullscreenEmitter } from './_layout';

export default function LiveNews() {
  const [region, setRegion] = useState<'india' | 'usa'>('india');
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [currentStreamIndex, setCurrentStreamIndex] = useState(0);

  useEffect(() => {
    setCurrentStreamIndex(0);
  }, [region]);

  const handleChannelSelect = async (streamIndex: number) => {
    setCurrentStreamIndex(streamIndex);
  };

  const currentStream = STREAMS[region][currentStreamIndex];

  return (
    <SafeAreaProvider>
      <View style={styles.safeArea}>
        <View style={styles.container}>
          {/* VideoPlayerSection rendered outside of hidden content to maintain fullscreen visibility */}
          <VideoPlayerSection
            streamUrl={currentStream}
            isFullscreen={isFullscreen}
            setIsFullscreen={setIsFullscreen}
            fullscreenEmitter={fullscreenEmitter}
          />
          <View style={[styles.contentArea, isFullscreen && styles.hiddenContent]}>
            <View style={styles.regionSelector}>
              <View style={styles.regionToggle}>
                <Pressable
                  style={[styles.regionButton, region === 'india' && styles.activeRegion]}
                  onPress={() => setRegion('india')}>
                  <Text style={[styles.regionText, region === 'india' && styles.activeRegionText]}>
                    India
                  </Text>
                </Pressable>
                <Pressable
                  style={[styles.regionButton, region === 'usa' && styles.activeRegion]}
                  onPress={() => setRegion('usa')}>
                  <Text style={[styles.regionText, region === 'usa' && styles.activeRegionText]}>
                    USA
                  </Text>
                </Pressable>
              </View>
            </View>
            <NewsChannelList
              region={region}
              onChannelSelect={handleChannelSelect}
            />
          </View>
        </View>
      </View>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#0A0E1A',
  },
  container: {
    flex: 1,
    backgroundColor: '#0A0E1A',
  },
  contentArea: {
    flex: 1,
        paddingBottom: 1,
  },
  hiddenContent: {
    display: 'none',
  },
  regionSelector: {
    paddingHorizontal: 20,
    // paddingVertical: ,
  },
  regionToggle: {
    flexDirection: 'row',
    backgroundColor: 'rgba(20, 24, 35, 0.7)',
    borderRadius: 12,
    padding: 4,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.08)',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 8,
  },
  regionButton: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 16,
    borderRadius: 8,
  },
  activeRegion: {
    backgroundColor: '#3B82F6',
  },
  regionText: {
    color: '#FFFFFF',
    textAlign: 'center',
    fontWeight: '600',
    fontSize: 16,
  },
  activeRegionText: {
    color: '#FFFFFF',
  },
});
