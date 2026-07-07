import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { RegisterStationForm } from './RegisterStationForm';
import { api } from '../api/client';

vi.mock('../api/client', () => ({
  api: { registerStation: vi.fn() },
}));

describe('RegisterStationForm', () => {
  beforeEach(() => vi.clearAllMocks());

  it('validates that id and name are required', async () => {
    const onRegistered = vi.fn();
    render(<RegisterStationForm onRegistered={onRegistered} />);

    await userEvent.click(screen.getByRole('button', { name: /add station/i }));

    expect(screen.getByRole('alert')).toHaveTextContent(/required/i);
    expect(api.registerStation).not.toHaveBeenCalled();
    expect(onRegistered).not.toHaveBeenCalled();
  });

  it('submits the station payload and calls onRegistered', async () => {
    vi.mocked(api.registerStation).mockResolvedValueOnce({
      id: '1', externalId: 'st-9', name: 'Airport', latitude: null, longitude: null,
      connectorType: 'CCS', powerKw: 150, pricePerKwh: 0.3,
      status: 'AVAILABLE', lastSeenAt: null, createdAt: '',
    });
    const onRegistered = vi.fn();
    render(<RegisterStationForm onRegistered={onRegistered} />);

    await userEvent.type(screen.getByLabelText(/station id/i), 'st-9');
    await userEvent.type(screen.getByLabelText(/station name/i), 'Airport');
    await userEvent.click(screen.getByRole('button', { name: /add station/i }));

    await waitFor(() =>
      expect(api.registerStation).toHaveBeenCalledWith(
        expect.objectContaining({ externalId: 'st-9', name: 'Airport', connectorType: 'CCS' }),
      ),
    );
    expect(onRegistered).toHaveBeenCalled();
  });
});
