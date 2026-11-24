import Ionicons from '@expo/vector-icons/Ionicons';
import { Redirect } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { ErrorApi } from '@/api';
import { useAuth } from '@/auth';
import { AvisoError, Boton, Campo, Pantalla } from '@/componentes/ui';
import { Semaforo } from '@/componentes/Semaforo';
import { cargarUrlServidor, guardarUrlServidor } from '@/config';
import { color, espacio, radio, texto } from '@/tema';

export default function Login() {
  const { sesion, entrar, registrarse } = useAuth();
  const [modo, setModo] = useState<'entrar' | 'registro'>('entrar');
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [url, setUrl] = useState('');
  const [verServidor, setVerServidor] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [campos, setCampos] = useState<Record<string, string>>({});

  useEffect(() => {
    cargarUrlServidor().then(setUrl);
  }, []);

  if (sesion) {
    return <Redirect href="/" />;
  }

  const enviar = async () => {
    setError(null);
    setCampos({});
    setEnviando(true);
    try {
      await guardarUrlServidor(url);
      if (modo === 'entrar') {
        await entrar(email.trim(), password);
      } else {
        await registrarse(nombre.trim(), email.trim(), password);
      }
    } catch (e) {
      if (e instanceof ErrorApi) {
        setError(e.message);
        setCampos(e.campos);
      } else {
        setError('Algo ha fallado. Inténtalo de nuevo.');
      }
    } finally {
      setEnviando(false);
    }
  };

  return (
    <Pantalla>
      <View style={estilos.marca}>
        <Semaforo valor="VERDE" />
        <Text style={estilos.nombreApp}>Luz Verde</Text>
        <Text style={[texto.suave, { textAlign: 'center' }]}>
          Tu asesor de empleo. Guarda candidaturas, evalúa ofertas y genera un CV para cada una.
        </Text>
      </View>

      <View style={estilos.selector}>
        {(['entrar', 'registro'] as const).map((m) => (
          <Pressable key={m} onPress={() => setModo(m)} style={[estilos.opcion, modo === m && estilos.opcionActiva]}>
            <Text style={[estilos.opcionTexto, modo === m && { color: color.primarioOscuro }]}>
              {m === 'entrar' ? 'Entrar' : 'Crear cuenta'}
            </Text>
          </Pressable>
        ))}
      </View>

      {process.env.EXPO_PUBLIC_DEMO === '1' ? (
        <View style={estilos.demo}>
          <Ionicons name="flask-outline" size={16} color={color.azul} />
          <Text style={[texto.suave, { color: color.azul, flex: 1 }]}>
            Modo demo: los datos son inventados. Entra con cualquier email y contraseña.
          </Text>
        </View>
      ) : null}

      <AvisoError mensaje={error} />

      {modo === 'registro' ? (
        <Campo etiqueta="Nombre" value={nombre} onChangeText={setNombre} placeholder="Tu nombre" error={campos.nombre} />
      ) : null}
      <Campo
        etiqueta="Email"
        value={email}
        onChangeText={setEmail}
        placeholder="tu@email.com"
        autoCapitalize="none"
        keyboardType="email-address"
        autoComplete="email"
        error={campos.email}
      />
      <Campo
        etiqueta="Contraseña"
        value={password}
        onChangeText={setPassword}
        placeholder={modo === 'registro' ? 'Mínimo 8 caracteres' : 'Tu contraseña'}
        secureTextEntry
        error={campos.password}
      />

      <Boton
        titulo={modo === 'entrar' ? 'Entrar' : 'Crear cuenta'}
        alPulsar={enviar}
        cargando={enviando}
        desactivado={!email || !password || (modo === 'registro' && !nombre)}
        estilo={{ marginTop: espacio.s }}
      />

      <Pressable onPress={() => setVerServidor((v) => !v)} style={estilos.servidor}>
        <Ionicons name="server-outline" size={16} color={color.textoSuave} />
        <Text style={texto.suave}>Servidor</Text>
        <Ionicons name={verServidor ? 'chevron-up' : 'chevron-down'} size={16} color={color.textoSuave} />
      </Pressable>
      {verServidor ? (
        <Campo
          etiqueta="URL del backend"
          value={url}
          onChangeText={setUrl}
          autoCapitalize="none"
          keyboardType="url"
          ayuda="En el emulador de Android, http://10.0.2.2:8080 apunta a tu ordenador."
        />
      ) : null}
    </Pantalla>
  );
}

const estilos = StyleSheet.create({
  marca: { alignItems: 'center', gap: espacio.m, marginTop: espacio.xxl, marginBottom: espacio.xl },
  nombreApp: { fontSize: 34, fontWeight: '900', color: color.primarioOscuro, letterSpacing: -1 },
  selector: {
    flexDirection: 'row',
    backgroundColor: color.superficieSuave,
    borderRadius: radio.m,
    padding: 4,
    marginBottom: espacio.l,
  },
  opcion: { flex: 1, paddingVertical: 10, alignItems: 'center', borderRadius: radio.s },
  opcionActiva: { backgroundColor: color.superficie },
  opcionTexto: { fontWeight: '700', color: color.textoSuave },
  demo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacio.s,
    backgroundColor: color.azulClaro,
    borderRadius: radio.m,
    padding: espacio.m,
    marginBottom: espacio.l,
  },
  servidor: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    marginTop: espacio.xl,
    marginBottom: espacio.m,
  },
});
