import { useFocusEffect } from 'expo-router';
import { useCallback, useState, type ReactNode } from 'react';
import { Alert, StyleSheet, Text, TextInput, View } from 'react-native';
import { api, ErrorApi } from '@/api';
import { useAuth } from '@/auth';
import { BarraProgreso } from '@/componentes/AvisoPerfil';
import { AvisoError, Boton, BotonIcono, Campo, Cargando, Chip, Pantalla, Seccion, Tarjeta } from '@/componentes/ui';
import { cargarUrlServidor, guardarUrlServidor, SERVIDOR_EDITABLE } from '@/config';
import { esMesValido, oNull } from '@/formato';
import { calcularProgreso } from '@/progresoPerfil';
import { color, espacio, radio, texto } from '@/tema';
import type { Experiencia, Formacion, Idioma, Modalidad, NivelTecnologia, Perfil, Proyecto } from '@/tipos';

const NIVELES: { valor: NivelTecnologia; nombre: string; color: string; fondo: string }[] = [
  { valor: 'SOLIDO', nombre: 'Sólido', color: color.primario, fondo: color.primarioClaro },
  { valor: 'MEDIO', nombre: 'Medio', color: color.ambar, fondo: color.ambarClaro },
  { valor: 'SIN_TOCAR', nombre: 'Sin tocar', color: color.gris, fondo: color.grisClaro },
];

const MODALIDADES: { valor: Modalidad; nombre: string }[] = [
  { valor: 'PRESENCIAL', nombre: 'Presencial' },
  { valor: 'HIBRIDO', nombre: 'Híbrido' },
  { valor: 'REMOTO', nombre: 'Remoto' },
];

const VACIO: Perfil = {
  titular: null,
  telefono: null,
  linkedin: null,
  github: null,
  salarioMinimo: null,
  modalidad: null,
  ubicacion: null,
  tecnologias: [],
  experiencias: [],
  formaciones: [],
  proyectos: [],
  idiomas: [],
};

export default function PantallaPerfil() {
  const { sesion, salir, borrarCuenta } = useAuth();
  const [perfil, setPerfil] = useState<Perfil | null>(null);
  const [salario, setSalario] = useState('');
  const [nuevaTec, setNuevaTec] = useState('');
  const [url, setUrl] = useState('');
  const [guardando, setGuardando] = useState(false);
  const [guardado, setGuardado] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useFocusEffect(
    useCallback(() => {
      cargarUrlServidor().then(setUrl);
      api
        .perfil()
        .then((p) => {
          setPerfil({ ...VACIO, ...p });
          setSalario(p.salarioMinimo ? String(p.salarioMinimo) : '');
        })
        .catch((e) => {
          setError(e instanceof ErrorApi ? e.message : 'No se ha podido cargar el perfil.');
          setPerfil((p) => p ?? VACIO);
        });
    }, []),
  );

  if (!perfil) {
    return (
      <Pantalla titulo="Perfil">
        <Cargando />
      </Pantalla>
    );
  }

  const progreso = calcularProgreso({ ...perfil, salarioMinimo: salario ? Number(salario) || null : null });

  const cambiar = (cambios: Partial<Perfil>) => {
    setGuardado(false);
    setPerfil({ ...perfil, ...cambios });
  };

  function editarLista<K extends 'experiencias' | 'formaciones' | 'proyectos' | 'idiomas'>(
    clave: K,
    indice: number,
    cambios: Partial<Perfil[K][number]>,
  ) {
    const lista = [...perfil![clave]] as Perfil[K][number][];
    lista[indice] = { ...lista[indice], ...cambios };
    cambiar({ [clave]: lista } as Partial<Perfil>);
  }

  function quitar<K extends keyof Perfil>(clave: K, indice: number) {
    const lista = (perfil![clave] as unknown[]).filter((_, i) => i !== indice);
    cambiar({ [clave]: lista } as Partial<Perfil>);
  }

  const anadirTecnologia = () => {
    const nombre = nuevaTec.trim();
    if (!nombre || perfil.tecnologias.some((t) => t.nombre.toLowerCase() === nombre.toLowerCase())) {
      setNuevaTec('');
      return;
    }
    cambiar({ tecnologias: [...perfil.tecnologias, { nombre, nivel: 'MEDIO' }] });
    setNuevaTec('');
  };

  const validar = (): string | null => {
    for (const e of perfil.experiencias) {
      if (!e.puesto.trim() || !e.empresa.trim()) return 'Cada experiencia necesita puesto y empresa.';
      if (!esMesValido(e.inicio) || (e.fin && !esMesValido(e.fin)))
        return 'Las fechas de experiencia van en formato AAAA-MM, por ejemplo 2026-02.';
    }
    for (const f of perfil.formaciones) {
      if (!f.titulo.trim()) return 'Cada formación necesita un título.';
      if ((f.inicio && !esMesValido(f.inicio)) || (f.fin && !esMesValido(f.fin)))
        return 'Las fechas de formación van en formato AAAA-MM.';
    }
    if (perfil.proyectos.some((p) => !p.nombre.trim())) return 'Cada proyecto necesita un nombre.';
    if (perfil.idiomas.some((i) => !i.nombre.trim())) return 'Cada idioma necesita un nombre.';
    if (salario && !/^\d+$/.test(salario.replace(/\./g, ''))) return 'El salario va en euros brutos al año, solo números.';
    return null;
  };

  const guardar = async () => {
    const problema = validar();
    if (problema) {
      setError(problema);
      return;
    }
    setError(null);
    setGuardando(true);
    const limpio: Perfil = {
      ...perfil,
      titular: oNull(perfil.titular),
      telefono: oNull(perfil.telefono),
      linkedin: oNull(perfil.linkedin),
      github: oNull(perfil.github),
      ubicacion: oNull(perfil.ubicacion),
      salarioMinimo: salario ? Number(salario.replace(/\./g, '')) : null,
      experiencias: perfil.experiencias.map((e) => ({ ...e, fin: oNull(e.fin), descripcion: oNull(e.descripcion) })),
      formaciones: perfil.formaciones.map((f) => ({ ...f, centro: oNull(f.centro), inicio: oNull(f.inicio), fin: oNull(f.fin) })),
      proyectos: perfil.proyectos.map((p) => ({
        ...p,
        descripcion: oNull(p.descripcion),
        enlace: oNull(p.enlace),
        tecnologias: oNull(p.tecnologias),
      })),
      idiomas: perfil.idiomas.map((i) => ({ ...i, nivel: oNull(i.nivel) })),
    };
    try {
      const guardadoServidor = await api.guardarPerfil(limpio);
      setPerfil({ ...VACIO, ...guardadoServidor });
      setGuardado(true);
    } catch (e) {
      if (e instanceof ErrorApi) {
        const primerCampo = Object.values(e.campos)[0];
        setError(primerCampo ? `${e.message}: ${primerCampo}` : e.message);
      } else {
        setError('No se ha podido guardar el perfil.');
      }
    } finally {
      setGuardando(false);
    }
  };

  const guardarServidor = async () => {
    const nueva = await guardarUrlServidor(url);
    setUrl(nueva);
    Alert.alert('Servidor guardado', nueva);
  };

  const confirmarBorrado = () =>
    Alert.alert(
      'Borrar cuenta',
      'Se borrarán tu cuenta, tu perfil, tus candidaturas, tus evaluaciones y tus CV. No se puede deshacer.',
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Borrar todo',
          style: 'destructive',
          onPress: async () => {
            try {
              await borrarCuenta();
            } catch (e) {
              setError(e instanceof ErrorApi ? e.message : 'No se ha podido borrar la cuenta.');
            }
          },
        },
      ],
    );

  const cerrarSesion = () =>
    Alert.alert('Cerrar sesión', '¿Quieres salir de tu cuenta?', [
      { text: 'Cancelar', style: 'cancel' },
      { text: 'Salir', style: 'destructive', onPress: () => void salir() },
    ]);

  return (
    <Pantalla titulo="Perfil" subtitulo={sesion?.nombre}>
      <Tarjeta estilo={{ marginBottom: espacio.l }}>
        <Text style={texto.etiqueta}>Perfil completo al {progreso.porcentaje} %</Text>
        <BarraProgreso porcentaje={progreso.porcentaje} />
        <Text style={[texto.suave, { marginTop: espacio.s, fontSize: 13 }]}>
          {progreso.pendientes.length
            ? `Siguiente: ${progreso.pendientes[0]}.`
            : 'Listo. La IA tiene todo lo que necesita.'}{' '}
          Nada que no esté aquí aparecerá en tu CV.
        </Text>
      </Tarjeta>

      <AvisoError mensaje={error} />

      <Seccion titulo="Datos">
        <Campo etiqueta="Titular del CV" value={perfil.titular ?? ''} onChangeText={(v) => cambiar({ titular: v })} placeholder="Desarrollador full stack junior" />
        <Campo etiqueta="Ubicación" value={perfil.ubicacion ?? ''} onChangeText={(v) => cambiar({ ubicacion: v })} placeholder="Sevilla" />
        <Campo etiqueta="Teléfono" value={perfil.telefono ?? ''} onChangeText={(v) => cambiar({ telefono: v })} keyboardType="phone-pad" />
        <Campo etiqueta="LinkedIn" value={perfil.linkedin ?? ''} onChangeText={(v) => cambiar({ linkedin: v })} autoCapitalize="none" placeholder="linkedin.com/in/..." />
        <Campo etiqueta="GitHub" value={perfil.github ?? ''} onChangeText={(v) => cambiar({ github: v })} autoCapitalize="none" placeholder="github.com/..." />
      </Seccion>

      <Seccion titulo="Lo que buscas">
        <Campo
          etiqueta="Salario mínimo (brutos al año)"
          value={salario}
          onChangeText={(v) => {
            setGuardado(false);
            setSalario(v);
          }}
          keyboardType="number-pad"
          placeholder="20000"
        />
        <Text style={[texto.etiqueta, { marginBottom: espacio.s }]}>Modalidad</Text>
        <View style={estilos.chips}>
          {MODALIDADES.map((m) => (
            <Chip
              key={m.valor}
              texto={m.nombre}
              activo={perfil.modalidad === m.valor}
              alPulsar={() => cambiar({ modalidad: perfil.modalidad === m.valor ? null : m.valor })}
            />
          ))}
        </View>
      </Seccion>

      <Seccion titulo="Stack">
        <View style={{ gap: espacio.s }}>
          {perfil.tecnologias.map((t, i) => (
            <View key={`${t.nombre}-${i}`} style={estilos.tec}>
              <Text style={[texto.cuerpo, { fontWeight: '700', flex: 1 }]}>{t.nombre}</Text>
              <View style={{ flexDirection: 'row', gap: 4 }}>
                {NIVELES.map((n) => (
                  <Chip
                    key={n.valor}
                    texto={n.nombre}
                    activo={t.nivel === n.valor}
                    colorActivo={n.color}
                    fondoActivo={n.fondo}
                    alPulsar={() => {
                      const tecnologias = [...perfil.tecnologias];
                      tecnologias[i] = { ...t, nivel: n.valor };
                      cambiar({ tecnologias });
                    }}
                  />
                ))}
              </View>
              <BotonIcono icono="close" etiqueta={`Quitar ${t.nombre}`} alPulsar={() => quitar('tecnologias', i)} colorIcono={color.textoTenue} />
            </View>
          ))}
        </View>
        <View style={estilos.nuevaTec}>
          <TextInput
            value={nuevaTec}
            onChangeText={setNuevaTec}
            placeholder="Añadir tecnología (Java, React...)"
            placeholderTextColor={color.textoTenue}
            style={estilos.inputTec}
            onSubmitEditing={anadirTecnologia}
            returnKeyType="done"
          />
          <BotonIcono icono="add" etiqueta="Añadir tecnología" alPulsar={anadirTecnologia} colorIcono={color.primario} />
        </View>
      </Seccion>

      <SeccionLista
        titulo="Experiencia"
        alAnadir={() => cambiar({ experiencias: [...perfil.experiencias, { puesto: '', empresa: '', inicio: '', fin: null, descripcion: null }] })}
      >
        {perfil.experiencias.map((e: Experiencia, i) => (
          <Elemento key={i} alQuitar={() => quitar('experiencias', i)}>
            <Campo etiqueta="Puesto" value={e.puesto} onChangeText={(v) => editarLista('experiencias', i, { puesto: v })} />
            <Campo etiqueta="Empresa" value={e.empresa} onChangeText={(v) => editarLista('experiencias', i, { empresa: v })} />
            <Fechas
              inicio={e.inicio}
              fin={e.fin}
              alCambiar={(inicio, fin) => editarLista('experiencias', i, { inicio, fin })}
              textoFin="Vacío si sigues"
            />
            <Campo
              etiqueta="Qué hiciste"
              value={e.descripcion ?? ''}
              multiline
              onChangeText={(v) => editarLista('experiencias', i, { descripcion: v })}
              placeholder="Una frase por tarea. Con datos reales."
            />
          </Elemento>
        ))}
      </SeccionLista>

      <SeccionLista
        titulo="Proyectos"
        alAnadir={() => cambiar({ proyectos: [...perfil.proyectos, { nombre: '', descripcion: null, enlace: null, tecnologias: null }] })}
      >
        {perfil.proyectos.map((p: Proyecto, i) => (
          <Elemento key={i} alQuitar={() => quitar('proyectos', i)}>
            <Campo etiqueta="Nombre" value={p.nombre} onChangeText={(v) => editarLista('proyectos', i, { nombre: v })} />
            <Campo
              etiqueta="Descripción"
              value={p.descripcion ?? ''}
              multiline
              onChangeText={(v) => editarLista('proyectos', i, { descripcion: v })}
            />
            <Campo
              etiqueta="Tecnologías"
              value={p.tecnologias ?? ''}
              onChangeText={(v) => editarLista('proyectos', i, { tecnologias: v })}
              placeholder="Spring Boot, React, PostgreSQL"
            />
            <Campo
              etiqueta="Enlace"
              value={p.enlace ?? ''}
              autoCapitalize="none"
              onChangeText={(v) => editarLista('proyectos', i, { enlace: v })}
              placeholder="github.com/..."
            />
          </Elemento>
        ))}
      </SeccionLista>

      <SeccionLista
        titulo="Formación"
        alAnadir={() => cambiar({ formaciones: [...perfil.formaciones, { titulo: '', centro: null, inicio: null, fin: null }] })}
      >
        {perfil.formaciones.map((f: Formacion, i) => (
          <Elemento key={i} alQuitar={() => quitar('formaciones', i)}>
            <Campo etiqueta="Título" value={f.titulo} onChangeText={(v) => editarLista('formaciones', i, { titulo: v })} />
            <Campo etiqueta="Centro" value={f.centro ?? ''} onChangeText={(v) => editarLista('formaciones', i, { centro: v })} />
            <Fechas
              inicio={f.inicio ?? ''}
              fin={f.fin}
              alCambiar={(inicio, fin) => editarLista('formaciones', i, { inicio, fin })}
              textoFin="Vacío si sigues"
            />
          </Elemento>
        ))}
      </SeccionLista>

      <SeccionLista titulo="Idiomas" alAnadir={() => cambiar({ idiomas: [...perfil.idiomas, { nombre: '', nivel: null }] })}>
        {perfil.idiomas.map((idioma: Idioma, i) => (
          <Elemento key={i} alQuitar={() => quitar('idiomas', i)}>
            <View style={{ flexDirection: 'row', gap: espacio.s }}>
              <View style={{ flex: 1 }}>
                <Campo etiqueta="Idioma" value={idioma.nombre} onChangeText={(v) => editarLista('idiomas', i, { nombre: v })} />
              </View>
              <View style={{ flex: 1 }}>
                <Campo etiqueta="Nivel" value={idioma.nivel ?? ''} onChangeText={(v) => editarLista('idiomas', i, { nivel: v })} placeholder="B1" />
              </View>
            </View>
          </Elemento>
        ))}
      </SeccionLista>

      <Boton
        titulo={guardado ? 'Perfil guardado' : 'Guardar perfil'}
        icono={guardado ? 'checkmark-done' : 'save-outline'}
        alPulsar={guardar}
        cargando={guardando}
        estilo={{ marginTop: espacio.xl }}
      />

      {SERVIDOR_EDITABLE ? (
        <Seccion titulo="Servidor">
          <Campo
            etiqueta="URL del backend"
            value={url}
            onChangeText={setUrl}
            autoCapitalize="none"
            keyboardType="url"
            ayuda="En el emulador de Android, http://10.0.2.2:8080 es tu ordenador."
          />
          <Boton titulo="Guardar servidor" variante="secundario" alPulsar={guardarServidor} />
        </Seccion>
      ) : null}

      <Boton titulo="Cerrar sesión" icono="log-out-outline" variante="peligro" alPulsar={cerrarSesion} estilo={{ marginTop: espacio.xl }} />
      <Boton
        titulo="Borrar cuenta y datos"
        icono="trash-outline"
        variante="texto"
        alPulsar={confirmarBorrado}
        estilo={{ marginTop: espacio.m }}
      />
    </Pantalla>
  );
}

function SeccionLista({ titulo, alAnadir, children }: { titulo: string; alAnadir: () => void; children: ReactNode }) {
  return (
    <Seccion titulo={titulo} accion={<Boton titulo="Añadir" icono="add" variante="texto" alPulsar={alAnadir} />}>
      <View style={{ gap: espacio.m }}>{children}</View>
    </Seccion>
  );
}

function Elemento({ children, alQuitar }: { children: ReactNode; alQuitar: () => void }) {
  return (
    <Tarjeta>
      {children}
      <Boton titulo="Quitar" icono="trash-outline" variante="peligro" alPulsar={alQuitar} />
    </Tarjeta>
  );
}

function Fechas({
  inicio,
  fin,
  alCambiar,
  textoFin,
}: {
  inicio: string;
  fin: string | null;
  alCambiar: (inicio: string, fin: string | null) => void;
  textoFin: string;
}) {
  return (
    <View style={{ flexDirection: 'row', gap: espacio.s }}>
      <View style={{ flex: 1 }}>
        <Campo
          etiqueta="Inicio"
          value={inicio}
          onChangeText={(v) => alCambiar(v, fin)}
          placeholder="AAAA-MM"
          keyboardType="numbers-and-punctuation"
        />
      </View>
      <View style={{ flex: 1 }}>
        <Campo
          etiqueta="Fin"
          value={fin ?? ''}
          onChangeText={(v) => alCambiar(inicio, v || null)}
          placeholder="AAAA-MM"
          keyboardType="numbers-and-punctuation"
          ayuda={textoFin}
        />
      </View>
    </View>
  );
}

const estilos = StyleSheet.create({
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: espacio.s },
  tec: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacio.s,
    backgroundColor: color.superficie,
    borderRadius: radio.m,
    borderWidth: 1,
    borderColor: color.borde,
    paddingLeft: espacio.m,
    paddingVertical: 6,
    paddingRight: 6,
    flexWrap: 'wrap',
  },
  nuevaTec: { flexDirection: 'row', gap: espacio.s, alignItems: 'center', marginTop: espacio.m },
  inputTec: {
    flex: 1,
    backgroundColor: color.superficie,
    borderWidth: 1,
    borderColor: color.borde,
    borderRadius: radio.m,
    paddingHorizontal: espacio.m,
    paddingVertical: 10,
    fontSize: 15,
    color: color.texto,
  },
});
