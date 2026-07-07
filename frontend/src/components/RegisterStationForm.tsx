import { useState, type FormEvent } from 'react';
import { api, type ConnectorType } from '../api/client';

interface Props {
  onRegistered: () => void;
}

const CONNECTORS: ConnectorType[] = ['CCS', 'TYPE2', 'CHADEMO', 'TESLA'];

export function RegisterStationForm({ onRegistered }: Props) {
  const [externalId, setExternalId] = useState('');
  const [name, setName] = useState('');
  const [connectorType, setConnectorType] = useState<ConnectorType>('CCS');
  const [powerKw, setPowerKw] = useState('150');
  const [pricePerKwh, setPricePerKwh] = useState('0.30');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!externalId.trim() || !name.trim()) {
      setError('Station ID and name are required.');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await api.registerStation({
        externalId: externalId.trim(),
        name: name.trim(),
        connectorType,
        powerKw: Number(powerKw),
        pricePerKwh: Number(pricePerKwh),
      });
      setExternalId('');
      setName('');
      onRegistered();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} aria-label="Register station" className="register-form">
      <input aria-label="Station ID" placeholder="Station ID" value={externalId}
             onChange={(e) => setExternalId(e.target.value)} />
      <input aria-label="Station name" placeholder="Name" value={name}
             onChange={(e) => setName(e.target.value)} />
      <select aria-label="Connector type" value={connectorType}
              onChange={(e) => setConnectorType(e.target.value as ConnectorType)}>
        {CONNECTORS.map((c) => <option key={c} value={c}>{c}</option>)}
      </select>
      <input aria-label="Power kW" type="number" min="1" placeholder="kW" value={powerKw}
             onChange={(e) => setPowerKw(e.target.value)} />
      <input aria-label="Price per kWh" type="number" min="0" step="0.01" placeholder="$/kWh"
             value={pricePerKwh} onChange={(e) => setPricePerKwh(e.target.value)} />
      <button type="submit" disabled={submitting}>
        {submitting ? 'Adding…' : 'Add station'}
      </button>
      {error && <p className="error" role="alert">{error}</p>}
    </form>
  );
}
