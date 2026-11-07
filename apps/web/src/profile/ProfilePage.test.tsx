import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { CurrentUser } from '../auth/AuthProvider';

const edition = vi.hoisted(() => ({ isDesktopEdition: false }));
vi.mock('../edition', () => edition);
const auth = vi.hoisted(() => ({ updateUser: vi.fn() }));
vi.mock('../auth/AuthProvider', () => ({ useAuth: () => ({ signIn: vi.fn(), signOut: vi.fn(), updateUser: auth.updateUser }) }));
const session = vi.hoisted(() => ({ redirectToAccountAction: vi.fn() }));
vi.mock('../auth/session', () => session);
const api = vi.hoisted(() => ({ saveDisplayName: vi.fn(), uploadAvatar: vi.fn(), removeAvatar: vi.fn() }));
vi.mock('./api', () => api);

import { ProfilePage } from './ProfilePage';

const USER: CurrentUser = {
  id: 'p1',
  subject: 's1',
  displayName: 'lydia',
  username: 'lydia',
  email: 'lydia@example.com',
  roles: ['player'],
  avatarUrl: null,
};

function renderProfile(user: CurrentUser = USER) {
  render(
    <MemoryRouter>
      <ProfilePage user={user} />
    </MemoryRouter>,
  );
}

describe('ProfilePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    edition.isDesktopEdition = false;
  });

  it('shows the username and email read-only, and the shown name as an editable field', () => {
    renderProfile({ ...USER, displayName: 'Lady Lydia' });

    expect(screen.getByLabelText('Display name')).toHaveValue('Lady Lydia');
    expect(screen.getByText('lydia')).toBeInTheDocument();
    expect(screen.getByText('lydia@example.com')).toBeInTheDocument();
  });

  it('saves the shown name and hands the updated player to the session', async () => {
    const saved = { ...USER, displayName: 'Lady Lydia' };
    api.saveDisplayName.mockResolvedValue(saved);
    renderProfile();

    fireEvent.change(screen.getByLabelText('Display name'), { target: { value: '  Lady Lydia ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save' }));

    await waitFor(() => expect(auth.updateUser).toHaveBeenCalledWith(saved));
    expect(api.saveDisplayName).toHaveBeenCalledWith('Lady Lydia');
    expect(screen.getByRole('status')).toHaveTextContent('Saved.');
  });

  it('refuses a file that is not a PNG or JPEG before sending it', () => {
    renderProfile();

    fireEvent.change(screen.getByTestId('avatar-file'), { target: { files: [new File(['x'], 'me.gif', { type: 'image/gif' })] } });

    expect(screen.getByRole('alert')).toHaveTextContent('Choose a PNG or JPEG image.');
    expect(api.uploadAvatar).not.toHaveBeenCalled();
  });

  it('uploads a picture and removes it again', async () => {
    const withPicture = { ...USER, avatarUrl: 'https://storage.test/me.png' };
    api.uploadAvatar.mockResolvedValue(withPicture);
    renderProfile();

    const file = new File(['x'], 'me.png', { type: 'image/png' });
    fireEvent.change(screen.getByTestId('avatar-file'), { target: { files: [file] } });
    await waitFor(() => expect(auth.updateUser).toHaveBeenCalledWith(withPicture));
    expect(api.uploadAvatar).toHaveBeenCalledWith(file);
  });

  it('opens the login service own screens to change the password or the email', () => {
    renderProfile();

    fireEvent.click(screen.getByRole('button', { name: 'Change password' }));
    fireEvent.click(screen.getByRole('button', { name: 'Change email' }));

    expect(session.redirectToAccountAction).toHaveBeenNthCalledWith(1, 'password', '/profile');
    expect(session.redirectToAccountAction).toHaveBeenNthCalledWith(2, 'email', '/profile');
  });

  it('has no password or email change in the desktop edition, where there is no login', () => {
    edition.isDesktopEdition = true;
    renderProfile();

    expect(screen.queryByRole('button', { name: 'Change password' })).toBeNull();
    expect(screen.getByLabelText('Display name')).toBeInTheDocument();
  });

  it('offers to remove a picture the player has', async () => {
    api.removeAvatar.mockResolvedValue(USER);
    renderProfile({ ...USER, avatarUrl: 'https://storage.test/me.png' });

    fireEvent.click(screen.getByRole('button', { name: 'Remove picture' }));

    await waitFor(() => expect(auth.updateUser).toHaveBeenCalledWith(USER));
  });
});
