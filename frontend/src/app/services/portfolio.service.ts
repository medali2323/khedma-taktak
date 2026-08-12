import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Certification,
  Education,
  Experience,
  Language,
  Portfolio,
  Project,
  PublishStatus,
  Skill,
  UserProfile,
} from '../models/portfolio.models';

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/portfolio';

  getPortfolio(): Observable<Portfolio> {
    return this.http.get<Portfolio>(this.base);
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.base}/profile`);
  }

  saveProfile(profile: UserProfile): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${this.base}/profile`, profile);
  }

  uploadPhoto(file: File): Observable<{ photoUrl: string | null }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ photoUrl: string | null }>(`${this.base}/profile/photo`, form);
  }

  getExperiences(): Observable<Experience[]> {
    return this.http.get<Experience[]>(`${this.base}/experiences`);
  }

  saveExperiences(items: Experience[]): Observable<Experience[]> {
    return this.http.put<Experience[]>(`${this.base}/experiences`, items);
  }

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.base}/projects`);
  }

  saveProjects(items: Project[]): Observable<Project[]> {
    return this.http.put<Project[]>(`${this.base}/projects`, items);
  }

  getEducation(): Observable<Education[]> {
    return this.http.get<Education[]>(`${this.base}/education`);
  }

  saveEducation(items: Education[]): Observable<Education[]> {
    return this.http.put<Education[]>(`${this.base}/education`, items);
  }

  getSkills(): Observable<Skill[]> {
    return this.http.get<Skill[]>(`${this.base}/skills`);
  }

  saveSkills(items: Skill[]): Observable<Skill[]> {
    return this.http.put<Skill[]>(`${this.base}/skills`, items);
  }

  getLanguages(): Observable<Language[]> {
    return this.http.get<Language[]>(`${this.base}/languages`);
  }

  saveLanguages(items: Language[]): Observable<Language[]> {
    return this.http.put<Language[]>(`${this.base}/languages`, items);
  }

  getCertifications(): Observable<Certification[]> {
    return this.http.get<Certification[]>(`${this.base}/certifications`);
  }

  saveCertifications(items: Certification[]): Observable<Certification[]> {
    return this.http.put<Certification[]>(`${this.base}/certifications`, items);
  }

  getPreviewHtml(): Observable<string> {
    return this.http.get('/api/render/preview', { responseType: 'text' });
  }

  getPublishStatus(): Observable<PublishStatus> {
    return this.http.get<PublishStatus>(`${this.base}/publish/status`);
  }

  publish(): Observable<PublishStatus> {
    return this.http.post<PublishStatus>(`${this.base}/publish`, {});
  }

  unpublish(): Observable<PublishStatus> {
    return this.http.post<PublishStatus>(`${this.base}/unpublish`, {});
  }
}
