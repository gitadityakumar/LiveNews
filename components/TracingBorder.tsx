import React, { useEffect } from 'react';
import { StyleSheet, Dimensions, View } from 'react-native';
import Svg, { Rect } from 'react-native-svg';
import Animated, {
  useAnimatedProps,
  useSharedValue,
  withRepeat,
  withTiming,
  Easing,
} from 'react-native-reanimated';

const AnimatedRect = Animated.createAnimatedComponent(Rect);

const TracingBorder = () => {
  const { width, height } = Dimensions.get('screen');
  const progress = useSharedValue(0);

  const strokeWidth = 3;
  const padding = strokeWidth / 2;
  const cornerRadius = 45; // Match physical curved screen corners

  // Correct perimeter for a rounded rectangle
  const rw = width - strokeWidth;
  const rh = height - strokeWidth;
  const perimeter = 2 * (rw + rh) - 8 * cornerRadius + 2 * Math.PI * cornerRadius;

  useEffect(() => {
    progress.value = withRepeat(
      withTiming(1, {
        duration: 3500, // Slightly slower for better visibility
        easing: Easing.linear,
      }),
      -1,
      false
    );
  }, []);

  const animatedProps = useAnimatedProps(() => ({
    strokeDashoffset: perimeter * (1 - progress.value),
  }));

  return (
    <View style={styles.container} pointerEvents="none">
      <Svg width={width} height={height}>
        <AnimatedRect
          x={padding}
          y={padding}
          width={rw}
          height={rh}
          rx={cornerRadius}
          ry={cornerRadius}
          stroke="#3B82F6"
          strokeWidth={strokeWidth}
          fill="none"
          strokeDasharray={`${perimeter}`}
          animatedProps={animatedProps}
          strokeLinecap="round"
        />
      </Svg>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    ...StyleSheet.absoluteFillObject,
    zIndex: 9999,
  },
});

export default TracingBorder;
