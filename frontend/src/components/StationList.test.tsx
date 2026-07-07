import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { StationList } from './StationList';
import type { Station } from '../api/client';

function station(overrides: Partial<Station> = {}): Station {
  return {
    id: '1',
    externalId: 'st-1',
    name: 'Downtown Garage',
    latitude: 40,
    longitude: -74,
    connectorType: 'CCS',
    powerKw: 150,
    pricePerKwh: 0.3,
    status: 'AVAILABLE',
    lastSeenAt: null,
    createdAt: new Date().toISOString(),
    ...overrides,
  };
}

describe('StationList', () => {
  it('shows an empty state when there are no stations', () => {
    render(<StationList stations={[]} onReserve={vi.fn()} onRelease={vi.fn()} />);
    expect(screen.getByText(/no charging stations/i)).toBeInTheDocument();
  });

  it('renders station name, specs and status', () => {
    render(<StationList stations={[station()]} onReserve={vi.fn()} onRelease={vi.fn()} />);
    expect(screen.getByText('Downtown Garage')).toBeInTheDocument();
    expect(screen.getByText('CCS')).toBeInTheDocument();
    expect(screen.getByText('150 kW')).toBeInTheDocument();
    expect(screen.getByText('$0.3/kWh')).toBeInTheDocument();
    expect(screen.getByText('AVAILABLE')).toBeInTheDocument();
  });

  it('enables Reserve only when available and calls handler', async () => {
    const onReserve = vi.fn();
    render(<StationList stations={[station()]} onReserve={onReserve} onRelease={vi.fn()} />);

    await userEvent.click(screen.getByRole('button', { name: /reserve/i }));
    expect(onReserve).toHaveBeenCalledWith('st-1');
  });

  it('disables Reserve when the station is charging', () => {
    render(<StationList stations={[station({ status: 'CHARGING' })]} onReserve={vi.fn()} onRelease={vi.fn()} />);
    expect(screen.getByRole('button', { name: /reserve/i })).toBeDisabled();
  });
});
