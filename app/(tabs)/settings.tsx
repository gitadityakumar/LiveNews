import { View, Text, StyleSheet, Switch, Pressable } from 'react-native';
import { useState } from 'react';

export default function Settings() {
  const [autoplay, setAutoplay] = useState(true);
  const [notifications, setNotifications] = useState(false);
  const [dataSaver, setDataSaver] = useState(false);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Settings</Text>
      
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Playback</Text>
        <View style={styles.setting}>
          <Text style={styles.settingText}>Autoplay</Text>
          <Switch
            value={autoplay}
            onValueChange={setAutoplay}
            trackColor={{ false: '#333', true: '#ff3b30' }}
            thumbColor={autoplay ? '#fff' : '#f4f3f4'}
          />
        </View>
        <View style={styles.setting}>
          <Text style={styles.settingText}>Data Saver</Text>
          <Switch
            value={dataSaver}
            onValueChange={setDataSaver}
            trackColor={{ false: '#333', true: '#ff3b30' }}
            thumbColor={dataSaver ? '#fff' : '#f4f3f4'}
          />
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Advance</Text>
        <View style={styles.setting}>
          <Text style={styles.settingText}>Sync Streams</Text>
          <Switch
            value={notifications}
            onValueChange={setNotifications}
            trackColor={{ false: '#333', true: '#ff3b30' }}
            thumbColor={notifications ? '#fff' : '#f4f3f4'}
          />
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>About</Text>
        <Pressable style={styles.button}>
          <Text style={styles.buttonText}>Privacy Policy</Text>
        </Pressable>
        <Pressable style={styles.button}>
          <Text style={styles.buttonText}>Terms of Service</Text>
        </Pressable>
        <Text style={styles.version}>Version 1.0.0</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
    paddingTop: 60,
    paddingHorizontal: 20,
  },
  title: {
    fontSize: 28,
    fontFamily: 'Inter-Bold',
    color: '#fff',
    marginBottom: 30,
  },
  section: {
    marginBottom: 30,
  },
  sectionTitle: {
    fontSize: 18,
    fontFamily: 'Inter-SemiBold',
    color: '#888',
    marginBottom: 15,
  },
  setting: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  settingText: {
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: '#fff',
  },
  button: {
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  buttonText: {
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: '#fff',
  },
  version: {
    marginTop: 20,
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#666',
    textAlign: 'center',
  },
});