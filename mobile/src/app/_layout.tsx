import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { ProveedorAuth } from '@/auth';
import { color } from '@/tema';

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <ProveedorAuth>
        <StatusBar style="dark" />
        <Stack
          screenOptions={{
            headerStyle: { backgroundColor: color.fondo },
            headerShadowVisible: false,
            headerTintColor: color.primarioOscuro,
            headerTitleStyle: { fontWeight: '700' },
            contentStyle: { backgroundColor: color.fondo },
          }}
        >
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen name="login" options={{ headerShown: false }} />
          <Stack.Screen name="candidatura/[id]" options={{ title: 'Candidatura' }} />
          <Stack.Screen name="evaluacion/[id]" options={{ title: 'Evaluación' }} />
        </Stack>
      </ProveedorAuth>
    </SafeAreaProvider>
  );
}
