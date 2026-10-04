import { useEffect, useState, type ReactNode } from 'react';
import { Navigate, Route, Routes, useLocation, useNavigate, useParams } from 'react-router';
import { useAuth } from '../auth/AuthProvider';
import { builderFor } from '../builder/builderDefinitions';
import { CharacterBuilder } from '../builder/CharacterBuilder';
import { CreateCharacterPage } from '../builder/CreateCharacterPage';
import { SystemPicker } from '../builder/SystemPicker';
import { getCharacter, type Character } from '../characters/api';
import { HomePage } from '../home/HomePage';
import { levelUpPageFor } from '../levelup/levelUpPages';
import { LandingPage } from '../home/LandingPage';
import { CharacterSheetScreen } from '../sheet/CharacterSheetScreen';
import { CreditsPage } from './CreditsPage';

export function AppShell() {
  const { status, user } = useAuth();
  const userLabel = user?.displayName ?? '...';
  return (
    <Routes>
      <Route path="/" element={<Home userLabel={userLabel} />} />
      <Route path="/credits" element={<CreditsPage userName={status === 'signed-in' ? userLabel : null} />} />
      <Route
        path="/characters/new"
        element={
          <RequireSession>
            <SystemPicker userName={userLabel} />
          </RequireSession>
        }
      />
      <Route
        path="/characters/new/:systemId"
        element={
          <RequireSession>
            <CreateCharacterPage userName={userLabel} />
          </RequireSession>
        }
      />
      <Route
        path="/characters/:id"
        element={
          <RequireSession>
            <SheetPage userLabel={userLabel} />
          </RequireSession>
        }
      />
      <Route
        path="/characters/:id/level-up"
        element={
          <RequireSession>
            <LevelUpRoute userLabel={userLabel} />
          </RequireSession>
        }
      />
      <Route
        path="/characters/:id/build/:step?"
        element={
          <RequireSession>
            <BuilderPage userLabel={userLabel} />
          </RequireSession>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

/** The landing page for visitors, the character list for a signed-in player. */
function Home({ userLabel }: { userLabel: string }) {
  const { status } = useAuth();
  if (status === 'loading') {
    return null;
  }
  return status === 'signed-in' ? <HomePage userName={userLabel} /> : <LandingPage />;
}

/** A page that needs a player: without a session it goes to the login and comes back to the same address. */
function RequireSession({ children }: { children: ReactNode }) {
  const { status, signIn } = useAuth();
  const location = useLocation();

  useEffect(() => {
    if (status === 'signed-out') {
      signIn(location.pathname + location.search);
    }
  }, [status, signIn, location.pathname, location.search]);

  return status === 'signed-in' ? <>{children}</> : null;
}

function SheetPage({ userLabel }: { userLabel: string }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const [character, setCharacter] = useState<Character | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    setCharacter(null);
    getCharacter(id!)
      .then(setCharacter)
      .catch(() => setFailed(true));
  }, [id]);

  if (failed) {
    return <Navigate to="/" replace />;
  }
  if (!character) {
    return <p>Loading…</p>;
  }
  if (character.status === 'DRAFT') {
    return <Navigate to={`/characters/${character.id}/build`} replace />;
  }
  return <CharacterSheetScreen key={character.id} character={character} userLabel={userLabel} onBack={() => navigate('/')} />;
}

/** The level up page of the character's own game system; back to the list when the system has none. */
function LevelUpRoute({ userLabel }: { userLabel: string }) {
  const { id } = useParams();
  const [character, setCharacter] = useState<Character | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    getCharacter(id!)
      .then(setCharacter)
      .catch(() => setFailed(true));
  }, [id]);

  if (failed) {
    return <Navigate to="/" replace />;
  }
  if (!character) {
    return <p>Loading…</p>;
  }
  const Page = levelUpPageFor(character.systemId);
  return Page ? <Page characterId={character.id} characterName={character.name} userName={userLabel} /> : <Navigate to={`/characters/${character.id}`} replace />;
}

function BuilderPage({ userLabel }: { userLabel: string }) {
  const { id, step } = useParams();
  return <CharacterBuilder key={id} characterId={id!} stepId={step} definitionFor={builderFor} userName={userLabel} />;
}
