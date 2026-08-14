import type { UserType } from './auth.models';

export type { UserType };

export interface UserProfile {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  title: string;
  summary: string;
  phone: string;
  location: string;
  website: string;
  linkedin: string;
  github: string;
  userType?: UserType;
  photoUrl?: string;
  primaryTrade?: string;
  tradeSpecialties?: string;
  drivingLicense?: string;
  hasOwnVehicle?: boolean;
  mobilityRadiusKm?: number | null;
  toolsEquipment?: string;
  studentInstitution?: string;
  studentYear?: string;
  internshipSought?: string;
}

export interface Experience {
  id?: number;
  company: string;
  position: string;
  location: string;
  startDate: string;
  endDate: string;
  current: boolean;
  description: string;
}

export interface Project {
  id?: number;
  name: string;
  description: string;
  url: string;
  technologies: string;
  startDate: string;
  endDate: string;
}

export interface Education {
  id?: number;
  institution: string;
  degree: string;
  field: string;
  startDate: string;
  endDate: string;
  description: string;
  educationType?: 'ACADEMIC' | 'VOCATIONAL';
}

export interface Skill {
  id?: number;
  name: string;
  level: string;
  category: string;
}

export interface Language {
  id?: number;
  name: string;
  proficiency: string;
}

export interface Certification {
  id?: number;
  name: string;
  issuer: string;
  date: string;
  url: string;
  certificationType?: 'PROFESSIONAL' | 'REGULATORY';
}

export interface Portfolio {
  id?: number;
  slug?: string;
  published: boolean;
  publishedAt?: string;
  profile?: UserProfile;
  experiences?: Experience[];
  projects?: Project[];
  education?: Education[];
  skills?: Skill[];
  languages?: Language[];
  certifications?: Certification[];
}

export interface PublishStatus {
  published: boolean;
  slug?: string;
  publishedAt?: string;
  previewUrl?: string;
}

export interface CvImportResult {
  profile: UserProfile;
  experiences: Experience[];
  projects: Project[];
  education: Education[];
  skills: Skill[];
  languages: Language[];
  certifications: Certification[];
  parserEngine?: string;
  parserNote?: string;
}

export interface CvImportProgressEvent {
  phase: 'extract' | 'parse' | 'complete' | 'error';
  progress: number;
  message: string;
  section?: string;
  found?: boolean;
  count?: number;
  result?: CvImportResult;
}

export interface CvExtractedPart {
  section: string;
  label: string;
  count?: number;
}

export interface TradeCategory {
  sector: string;
  code: string;
  labelFr: string;
  labelEn: string;
}

export interface AtsAnalyzeRequest {
  mode: 'OFFICE' | 'TRADES';
  targetDomain: string;
}

export interface AtsAnalysisResult {
  score: number;
  mode: string;
  targetDomain: string;
  strengths: string[];
  weaknesses: string[];
  recommendations: string[];
  suggestedKeywords: string[];
}

export interface AtsOptimizeResult {
  analysis: AtsAnalysisResult;
  optimizedProfile: UserProfile;
}

export type WizardStep =
  | 'profile'
  | 'experiences'
  | 'projects'
  | 'education'
  | 'skills'
  | 'languages'
  | 'certifications'
  | 'summary'
  | 'publish';

export const WIZARD_STEPS: { key: WizardStep; label: string }[] = [
  { key: 'profile', label: 'Profil' },
  { key: 'experiences', label: 'Expériences' },
  { key: 'projects', label: 'Projets' },
  { key: 'education', label: 'Formation' },
  { key: 'skills', label: 'Compétences' },
  { key: 'languages', label: 'Langues' },
  { key: 'certifications', label: 'Certifications' },
  { key: 'summary', label: 'Récapitulatif' },
  { key: 'publish', label: 'Publication' },
];
