import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Portrait } from '../characters/portrait';
import { PortraitPicker } from './PortraitPicker';

const api = vi.hoisted(() => ({
  choosePresetPortrait: vi.fn(),
  uploadPortrait: vi.fn(),
  removePortrait: vi.fn(),
}));

vi.mock('../characters/portrait', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../characters/portrait')>()),
  ...api,
}));

vi.mock('../characters/portraitPresets', () => ({
  portraitPresetsFor: () => [
    { id: '10064', url: '/p/10064.png' },
    { id: '10065', url: '/p/10065.png' },
  ],
  portraitImageUrl: (portrait: Portrait | null) =>
    portrait?.kind === 'UPLOAD' ? portrait.url : portrait ? `/p/${portrait.presetId}.png` : null,
}));

const PRESET: Portrait = { kind: 'PRESET', presetId: '10065', url: null };

function renderPicker(portrait: Portrait | null = null) {
  const onChange = vi.fn();
  render(<PortraitPicker characterId="c1" systemId="dnd-5e" portrait={portrait} onChange={onChange} />);
  return onChange;
}

describe('PortraitPicker', () => {
  beforeEach(() => vi.clearAllMocks());

  it('keeps the default portraits in a separate section opened by a button', () => {
    renderPicker(PRESET);
    expect(screen.queryByRole('radio', { name: 'Default portrait 2' })).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Pick a default portrait' }));

    expect(screen.getByRole('region', { name: 'Default portraits' })).toBeInTheDocument();
    expect(screen.getByRole('radio', { name: 'Default portrait 2' })).toHaveAttribute('aria-checked', 'true');
    expect(screen.getByRole('img', { name: 'Current portrait' })).toHaveAttribute('src', '/p/10065.png');
  });

  it('picks a default portrait and closes the section', async () => {
    api.choosePresetPortrait.mockResolvedValue({ kind: 'PRESET', presetId: '10064', url: null });
    const onChange = renderPicker();
    fireEvent.click(screen.getByRole('button', { name: 'Pick a default portrait' }));

    fireEvent.click(screen.getByRole('radio', { name: 'Default portrait 1' }));

    await waitFor(() => expect(onChange).toHaveBeenCalledWith({ kind: 'PRESET', presetId: '10064', url: null }));
    expect(api.choosePresetPortrait).toHaveBeenCalledWith('c1', '10064');
    expect(screen.queryByRole('region', { name: 'Default portraits' })).not.toBeInTheDocument();
  });

  it('uploads a PNG', async () => {
    const uploaded: Portrait = { kind: 'UPLOAD', presetId: null, url: 'http://minio/x.png?sig' };
    api.uploadPortrait.mockResolvedValue(uploaded);
    const onChange = renderPicker();
    const file = new File([new Uint8Array([1, 2])], 'me.png', { type: 'image/png' });

    fireEvent.change(screen.getByLabelText('Portrait image file'), { target: { files: [file] } });

    await waitFor(() => expect(onChange).toHaveBeenCalledWith(uploaded));
    expect(api.uploadPortrait).toHaveBeenCalledWith('c1', file);
  });

  it('refuses a GIF before sending it', () => {
    const onChange = renderPicker();

    fireEvent.change(screen.getByLabelText('Portrait image file'), {
      target: { files: [new File([new Uint8Array([1])], 'me.gif', { type: 'image/gif' })] },
    });

    expect(screen.getByRole('alert')).toHaveTextContent('Choose a PNG or JPEG image.');
    expect(api.uploadPortrait).not.toHaveBeenCalled();
    expect(onChange).not.toHaveBeenCalled();
  });

  it('shows the API problem when an upload is rejected', async () => {
    api.uploadPortrait.mockRejectedValue(new Error('The portrait could not be read as an image.'));
    renderPicker();

    fireEvent.change(screen.getByLabelText('Portrait image file'), {
      target: { files: [new File([new Uint8Array([1])], 'me.png', { type: 'image/png' })] },
    });

    expect(await screen.findByRole('alert')).toHaveTextContent('The portrait could not be read as an image.');
  });

  it('removes the portrait', async () => {
    api.removePortrait.mockResolvedValue(undefined);
    const onChange = renderPicker(PRESET);

    fireEvent.click(screen.getByRole('button', { name: 'Remove' }));

    await waitFor(() => expect(onChange).toHaveBeenCalledWith(null));
    expect(api.removePortrait).toHaveBeenCalledWith('c1');
  });
});
