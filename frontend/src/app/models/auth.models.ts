export type UserType = 'CANDIDATE' | 'STUDENT' | 'TRADES_WORKER';

export interface UserTypeOption {
  value: UserType;
  label: string;
}

export const USER_TYPE_OPTIONS: UserTypeOption[] = [
  { value: 'CANDIDATE', label: 'Candidat' },
  { value: 'STUDENT', label: 'Étudiant' },
  { value: 'TRADES_WORKER', label: 'Métier manuel' },
];

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  userType: UserType;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  userType: UserType;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface StoredUser {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  userType: UserType;
}
