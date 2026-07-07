export type StationStatus = 'AVAILABLE' | 'RESERVED' | 'CHARGING' | 'FAULTED' | 'OFFLINE';
export type ConnectorType = 'TYPE2' | 'CCS' | 'CHADEMO' | 'TESLA';

export interface Station {
  id: string;
  externalId: string;
  name: string;
  latitude: number | null;
  longitude: number | null;
  connectorType: ConnectorType | null;
  powerKw: number | null;
  pricePerKwh: number | null;
  status: StationStatus;
  lastSeenAt: string | null;
  createdAt: string;
}

export interface ChargingSession {
  id: string;
  stationExternalId: string;
  energyKwh: number;
  cost: number | null;
  status: 'ACTIVE' | 'COMPLETED';
  startedAt: string;
  endedAt: string | null;
}

export interface NewStation {
  externalId: string;
  name: string;
  connectorType: ConnectorType;
  powerKw: number;
  pricePerKwh: number;
}

const BASE = '/api/v1';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...init,
  });
  if (!res.ok) {
    const body = await res.text();
    throw new Error(`${res.status} ${res.statusText}: ${body}`);
  }
  return res.status === 204 || res.status === 202 ? (undefined as T) : ((await res.json()) as T);
}

export const api = {
  listStations: () => request<Station[]>('/stations'),
  getStation: (id: string) => request<Station>(`/stations/${id}`),
  registerStation: (s: NewStation) =>
    request<Station>('/stations', { method: 'POST', body: JSON.stringify(s) }),
  reserve: (id: string) => request<Station>(`/stations/${id}/reserve`, { method: 'POST' }),
  release: (id: string) => request<Station>(`/stations/${id}/release`, { method: 'POST' }),
  getSessions: (id: string, limit = 50) =>
    request<ChargingSession[]>(`/stations/${id}/sessions?limit=${limit}`),
  getRevenue: (id: string) => request<{ totalRevenue: number }>(`/stations/${id}/revenue`),
  sendCommand: (id: string, command: 'START' | 'STOP') =>
    request<void>(`/stations/${id}/commands`, {
      method: 'POST',
      body: JSON.stringify({ command }),
    }),
};
