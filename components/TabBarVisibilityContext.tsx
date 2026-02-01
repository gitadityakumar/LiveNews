import { createContext, useContext, useState, ReactNode } from 'react';

interface TabBarVisibilityContextType {
  isTabBarVisible: boolean;
  setTabBarVisible: (visible: boolean) => void;
  lastScrollY: number;
  setLastScrollY: (y: number) => void;
}

const TabBarVisibilityContext = createContext<TabBarVisibilityContextType | undefined>(undefined);

export function TabBarVisibilityProvider({ children }: { children: ReactNode }) {
  const [isTabBarVisible, setTabBarVisible] = useState(true);
  const [lastScrollY, setLastScrollY] = useState(0);

  return (
    <TabBarVisibilityContext.Provider
      value={{
        isTabBarVisible,
        setTabBarVisible,
        lastScrollY,
        setLastScrollY,
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
