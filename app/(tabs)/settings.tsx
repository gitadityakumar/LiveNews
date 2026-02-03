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
            trackColor={{ false: '#4B5563', true: '#3B82F6' }}
            thumbColor={autoplay ? '#FFFFFF' : '#4B5563'}
          />
        </View>
        <View style={styles.setting}>
          <Text style={styles.settingText}>Data Saver</Text>
          <Switch
            value={dataSaver}
            onValueChange={setDataSaver}
            trackColor={{ false: '#4B5563', true: '#3B82F6' }}
            thumbColor={dataSaver ? '#FFFFFF' : '#4B5563'}
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
            trackColor={{ false: '#4B5563', true: '#3B82F6' }}
            thumbColor={notifications ? '#FFFFFF' : '#4B5563'}
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
        <Text style={styles.version}>Version 1.0.1</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0A0E1A',
    paddingTop: 44,
    paddingHorizontal: 20,
    paddingBottom: 100,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#FFFFFF',
    marginBottom: 32,
    lineHeight: 34,
  },
  section: {
    marginBottom: 32,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '500',
    color: '#6B7280',
    textTransform: 'capitalize',
    marginBottom: 12,
    marginLeft: 20,
  },
  setting: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 16,
    paddingHorizontal: 20,
    backgroundColor: 'rgba(26, 31, 46, 0.7)',
    borderRadius: 16,
    marginBottom: 12,
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
  settingText: {
    fontSize: 16,
    fontWeight: '500',
    color: '#FFFFFF',
    flex: 1,
  },
  button: {
    paddingVertical: 16,
    paddingHorizontal: 20,
    backgroundColor: 'rgba(26, 31, 46, 0.7)',
    borderRadius: 16,
    marginBottom: 12,
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
  buttonText: {
    fontSize: 16,
    fontWeight: '500',
    color: '#FFFFFF',
  },
  version: {
    marginTop: 32,
    fontSize: 14,
    fontWeight: '500',
    color: '#6B7280',
    textAlign: 'center',
  },
});