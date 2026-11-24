import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { almacen } from './almacen';
import { alExpirarSesion, api, fijarToken } from './api';
import type { Sesion } from './tipos';

const CLAVE_SESION = 'luzverde.sesion';

interface ContextoAuth {
  sesion: Sesion | null;
  cargando: boolean;
  entrar: (email: string, password: string) => Promise<void>;
  registrarse: (nombre: string, email: string, password: string) => Promise<void>;
  salir: () => Promise<void>;
  borrarCuenta: () => Promise<void>;
}

const Contexto = createContext<ContextoAuth | null>(null);

export function ProveedorAuth({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<Sesion | null>(null);
  const [cargando, setCargando] = useState(true);

  const guardar = useCallback(async (nueva: Sesion | null) => {
    fijarToken(nueva?.token ?? null);
    setSesion(nueva);
    if (nueva) {
      await almacen.guardar(CLAVE_SESION, JSON.stringify(nueva));
    } else {
      await almacen.borrar(CLAVE_SESION);
    }
  }, []);

  useEffect(() => {
    (async () => {
      try {
        const guardada = await almacen.leer(CLAVE_SESION);
        if (guardada) {
          const s = JSON.parse(guardada) as Sesion;
          fijarToken(s.token);
          setSesion(s);
        }
      } finally {
        setCargando(false);
      }
    })();
  }, []);

  useEffect(() => {
    alExpirarSesion(() => {
      void guardar(null);
    });
  }, [guardar]);

  const valor = useMemo<ContextoAuth>(
    () => ({
      sesion,
      cargando,
      entrar: async (email, password) => guardar(await api.login(email, password)),
      registrarse: async (nombre, email, password) => guardar(await api.registro(nombre, email, password)),
      salir: () => guardar(null),
      borrarCuenta: async () => {
        await api.borrarCuenta();
        await guardar(null);
      },
    }),
    [sesion, cargando, guardar],
  );

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>;
}

export function useAuth(): ContextoAuth {
  const ctx = useContext(Contexto);
  if (!ctx) throw new Error('useAuth fuera de ProveedorAuth');
  return ctx;
}
