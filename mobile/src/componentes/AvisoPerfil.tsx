import Ionicons from '@expo/vector-icons/Ionicons';
import { router, useFocusEffect } from 'expo-router';
import { useCallback, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { api } from '../api';
import { calcularProgreso, type ProgresoPerfil } from '../progresoPerfil';
import { color, espacio, radio } from '../tema';

/** Tarjeta que invita a completar el perfil mientras no esté bastante lleno. */
export function AvisoPerfil({ umbral = 65 }: { umbral?: number }) {
  const [progreso, setProgreso] = useState<ProgresoPerfil | null>(null);

  useFocusEffect(
    useCallback(() => {
      api
        .perfil()
        .then((p) => setProgreso(calcularProgreso(p)))
        .catch(() => setProgreso(null));
    }, []),
  );

  if (!progreso || progreso.porcentaje >= umbral) return null;

  return (
    <Pressable
      onPress={() => router.push('/perfil')}
      accessibilityRole="button"
      accessibilityLabel={`Tu perfil está al ${progreso.porcentaje} por ciento. Completa ${progreso.pendientes[0]}.`}
      style={({ pressed }) => [estilos.caja, pressed && { opacity: 0.85 }]}
    >
      <View style={estilos.icono}>
        <Ionicons name="person-add-outline" size={20} color={color.primarioOscuro} />
      </View>
      <View style={{ flex: 1 }}>
        <Text style={estilos.titulo}>Completa tu perfil ({progreso.porcentaje} %)</Text>
        <Text style={estilos.texto}>Falta {progreso.pendientes[0]}. Sin esto la IA no puede ayudarte bien.</Text>
        <BarraProgreso porcentaje={progreso.porcentaje} />
      </View>
      <Ionicons name="chevron-forward" size={18} color={color.primarioOscuro} />
    </Pressable>
  );
}

export function BarraProgreso({ porcentaje }: { porcentaje: number }) {
  return (
    <View
      style={estilos.barra}
      accessibilityRole="progressbar"
      accessibilityValue={{ min: 0, max: 100, now: porcentaje }}
    >
      <View style={[estilos.relleno, { width: `${Math.max(4, porcentaje)}%` }]} />
    </View>
  );
}

const estilos = StyleSheet.create({
  caja: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacio.m,
    backgroundColor: color.primarioClaro,
    borderRadius: radio.l,
    padding: espacio.l,
    marginBottom: espacio.l,
  },
  icono: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: color.superficie,
    alignItems: 'center',
    justifyContent: 'center',
  },
  titulo: { fontWeight: '800', color: color.primarioOscuro, fontSize: 15 },
  texto: { color: color.primarioOscuro, fontSize: 13, marginTop: 2, lineHeight: 18 },
  barra: {
    height: 6,
    borderRadius: 3,
    backgroundColor: color.primarioTenue,
    marginTop: espacio.s,
    overflow: 'hidden',
  },
  relleno: { height: 6, borderRadius: 3, backgroundColor: color.primario },
});
