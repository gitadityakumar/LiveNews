import { createContext, useContext, useState, ReactNode, useRef, useEffect } from 'react';

interface TabBarVisibilityContextType {
  isTabBarVisible: boolean;
  setTabBarVisible: (visible: boolean) => void;
  lastScrollY: number;
  setLastScrollY: (y: number) => void;
  scrollDirection: 'up' | 'down';
  setScrollDirection: (direction: 'up' | 'down') => void;
}

const TabBarVisibilityContext = createContext<TabBarVisibilityContextType | undefined>(undefined);

export function TabBarVisibilityProvider({ children }: { children: ReactNode }) {
  const [isTabBarVisible, setTabBarVisible] = useState(true);
  const [lastScrollY, setLastScrollY] = useState(0);
  const [scrollDirection, setScrollDirection] = useState<'up' | 'down'>('down');

  // Auto-hide/show tab bar based on scroll direction
  useEffect(() => {
    if (scrollDirection === 'up') {
      setTabBarVisible(false);
    } else {
      setTabBarVisible(true);
    }
  }, [scrollDirection]);

  return (
    <TabBarVisibilityContext.Provider
      value={{
        isTabBarVisible,
        setTabBarVisible,
        lastScrollY,
        setLastScrollY,
        scrollDirection,
        setScrollDirection,
      }}
    >
      {children}
    </TabBarVisibilityContext.Provider>
  );
}

export function useTabBarVisibility() {
  const context = useContext(TabBarVisibilityContext);
  if (context === undefined) {
    throw new Error('useTabBarVisibility must be used within a TabBarVisibilityProvider');
  }
  return context;
}
