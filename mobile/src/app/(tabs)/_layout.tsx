import Ionicons from '@expo/vector-icons/Ionicons';
import { Redirect, Tabs } from 'expo-router';
import { useAuth } from '@/auth';
import { Cargando } from '@/componentes/ui';
import { color } from '@/tema';

export default function LayoutPestanas() {
  const { sesion, cargando } = useAuth();

  if (cargando) return <Cargando />;
  if (!sesion) return <Redirect href="/login" />;

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: color.primario,
        tabBarInactiveTintColor: color.textoTenue,
        tabBarStyle: { backgroundColor: color.superficie, borderTopColor: color.borde, height: 64, paddingTop: 6 },
        tabBarLabelStyle: { fontSize: 12, fontWeight: '600', marginBottom: 6 },
      }}
    >
      <Tabs.Screen
        name="index"
        options={{
          title: 'Candidaturas',
          tabBarIcon: ({ color: c, size }) => <Ionicons name="briefcase-outline" size={size} color={c as string} />,
        }}
      />
      <Tabs.Screen
        name="evaluar"
        options={{
          title: 'Evaluar',
          tabBarIcon: ({ color: c, size }) => <Ionicons name="speedometer-outline" size={size} color={c as string} />,
        }}
      />
      <Tabs.Screen
        name="cv"
        options={{
          title: 'CV',
          tabBarIcon: ({ color: c, size }) => <Ionicons name="document-text-outline" size={size} color={c as string} />,
        }}
      />
      <Tabs.Screen
        name="perfil"
        options={{
          title: 'Perfil',
          tabBarIcon: ({ color: c, size }) => <Ionicons name="person-circle-outline" size={size} color={c as string} />,
        }}
      />
    </Tabs>
  );
}
