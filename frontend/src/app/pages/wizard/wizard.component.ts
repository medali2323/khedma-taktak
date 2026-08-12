import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { USER_TYPE_OPTIONS } from '../../models/auth.models';
import {
  Certification,
  Education,
  Experience,
  Language,
  Project,
  PublishStatus,
  Skill,
  UserProfile,
  WIZARD_STEPS,
  WizardStep,
} from '../../models/portfolio.models';
import { AuthService } from '../../services/auth.service';
import { PortfolioService } from '../../services/portfolio.service';

const PHONE_PATTERN = /^\d{8}$/;

type ProfileField =
  | 'firstName'
  | 'lastName'
  | 'title'
  | 'email'
  | 'phone'
  | 'primaryTrade'
  | 'studentInstitution';

const PROFILE_FIELD_LABELS: Record<ProfileField, string> = {
  firstName: 'Prénom',
  lastName: 'Nom',
  title: 'Titre professionnel',
  email: 'Email',
  phone: 'Téléphone',
  primaryTrade: 'Métier principal',
  studentInstitution: 'Établissement',
};

@Component({
  selector: 'app-wizard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="container wizard-layout">
      <aside class="card wizard-sidebar">
        <h2>Mon profil candidat</h2>
        <nav class="step-nav">
          @for (step of steps; track step.key; let i = $index) {
            <button type="button" class="step-item" [class.active]="currentStep === step.key" (click)="goTo(step.key)">
              <span class="step-num">{{ i + 1 }}</span> {{ step.label }}
            </button>
          }
        </nav>
        <a routerLink="/dashboard" class="back-link">← Tableau de bord</a>
      </aside>

      <div class="card wizard-main">
        @if (message) { <div class="alert alert-success">{{ message }}</div> }
        @if (error) { <div class="alert alert-error">{{ error }}</div> }
        @if (portfolioLoading) {
          <p class="text-muted portfolio-loading">Synchronisation des données…</p>
        }
        <div class="wizard-step" [style.display]="isStepVisible('profile') ? 'block' : 'none'">
              <h2>Profil</h2>
              <p class="text-muted mb-2">Renseignez vos informations — enregistrez tout à la fin du parcours.</p>
              <form [formGroup]="profileForm">
                <div class="form-group">
                  <label for="userType">Type de profil</label>
                  <select id="userType" formControlName="userType">
                    @for (opt of userTypeOptions; track opt.value) {
                      <option [value]="opt.value">{{ opt.label }}</option>
                    }
                  </select>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label for="firstName">Prénom</label>
                    <input id="firstName" formControlName="firstName" autocomplete="given-name" />
                    @if (showProfileError('firstName')) {
                      <div class="error">{{ profileErrorMessage('firstName') }}</div>
                    }
                  </div>
                  <div class="form-group">
                    <label for="lastName">Nom</label>
                    <input id="lastName" formControlName="lastName" autocomplete="family-name" />
                    @if (showProfileError('lastName')) {
                      <div class="error">{{ profileErrorMessage('lastName') }}</div>
                    }
                  </div>
                </div>
                <div class="form-group">
                  <label for="title">Titre professionnel</label>
                  <input id="title" formControlName="title" />
                  @if (showProfileError('title')) {
                    <div class="error">{{ profileErrorMessage('title') }}</div>
                  }
                </div>
                <div class="form-group">
                  <label for="summary">Résumé</label>
                  <textarea id="summary" formControlName="summary" rows="4"></textarea>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label for="email">Email</label>
                    <input id="email" type="email" formControlName="email" autocomplete="email" />
                    @if (showProfileError('email')) {
                      <div class="error">{{ profileErrorMessage('email') }}</div>
                    }
                  </div>
                  <div class="form-group">
                    <label for="phone">Téléphone</label>
                    <input id="phone" type="tel" formControlName="phone" maxlength="8" inputmode="numeric" autocomplete="tel" placeholder="12345678" (input)="onPhoneInput($event)" />
                    @if (showProfileError('phone')) {
                      <div class="error">{{ profileErrorMessage('phone') }}</div>
                    }
                  </div>
                </div>
                <div class="form-group">
                  <label for="location">Localisation</label>
                  <input id="location" formControlName="location" />
                </div>
                @if (profileForm.controls.userType.value === 'TRADES_WORKER') {
                  <div class="form-group">
                    <label for="primaryTrade">Métier principal</label>
                    <input id="primaryTrade" formControlName="primaryTrade" />
                    @if (showProfileError('primaryTrade')) {
                      <div class="error">{{ profileErrorMessage('primaryTrade') }}</div>
                    }
                  </div>
                }
                @if (profileForm.controls.userType.value === 'STUDENT') {
                  <div class="form-group">
                    <label for="studentInstitution">Établissement</label>
                    <input id="studentInstitution" formControlName="studentInstitution" />
                    @if (showProfileError('studentInstitution')) {
                      <div class="error">{{ profileErrorMessage('studentInstitution') }}</div>
                    }
                  </div>
                }
              </form>
        </div>
        <div class="wizard-step" [style.display]="isStepVisible('experiences') ? 'block' : 'none'">
              <h2>Expériences</h2>
              <form [formGroup]="experiencesForm">
                <div formArrayName="items">
                  @for (group of experienceControls; track $index; let i = $index) {
                    <div class="list-item" [formGroupName]="i">
                      <div class="form-group"><label>Entreprise</label><input formControlName="company" /></div>
                      <div class="form-group"><label>Poste</label><input formControlName="position" /></div>
                      <div class="form-row">
                        <div class="form-group"><label>Début</label><input formControlName="startDate" type="date" /></div>
                        <div class="form-group"><label>Fin</label><input formControlName="endDate" type="date" /></div>
                      </div>
                      <div class="form-group"><label>Description</label><textarea formControlName="description" rows="2"></textarea></div>
                      <button type="button" class="btn btn-secondary btn-sm" (click)="removeExperience(i)">Supprimer</button>
                    </div>
                  }
                </div>
                <button type="button" class="btn btn-secondary" (click)="addExperience()">Ajouter une expérience</button>
              </form>
        </div>
        <div class="wizard-step" [style.display]="isStepVisible('projects') ? 'block' : 'none'">
              <h2>Projets</h2>
              <form [formGroup]="projectsForm">
                <div formArrayName="items">
                  @for (group of projectControls; track $index; let i = $index) {
                    <div class="list-item" [formGroupName]="i">
                      <div class="form-group"><label>Nom</label><input formControlName="name" /></div>
                      <div class="form-group"><label>Description</label><textarea formControlName="description" rows="2"></textarea></div>
                      <div class="form-group"><label>URL</label><input formControlName="url" /></div>
                      <div class="form-group"><label>Technologies</label><input formControlName="technologies" placeholder="Angular, Java…" /></div>
                      <div class="form-row">
                        <div class="form-group"><label>Début</label><input formControlName="startDate" type="month" /></div>
                        <div class="form-group"><label>Fin</label><input formControlName="endDate" type="month" /></div>
                      </div>
                      <button type="button" class="btn btn-secondary btn-sm" (click)="removeProject(i)">Supprimer</button>
                    </div>
                  }
                </div>
                <button type="button" class="btn btn-secondary" (click)="addProject()">Ajouter un projet</button>
              </form>
        </div>
        <div class="wizard-step" [style.display]="isStepVisible('education') ? 'block' : 'none'">
              <h2>Formation</h2>
              <form [formGroup]="educationForm">
                <div formArrayName="items">
                  @for (group of educationControls; track $index; let i = $index) {
                    <div class="list-item" [formGroupName]="i">
                      <div class="form-group">
                        <label>Type</label>
                        <select formControlName="educationType">
                          <option value="ACADEMIC">Académique</option>
                          <option value="VOCATIONAL">Professionnelle (CAP, BP…)</option>
                        </select>
                      </div>
                      <div class="form-group"><label>Établissement</label><input formControlName="institution" /></div>
                      <div class="form-row">
                        <div class="form-group"><label>Diplôme</label><input formControlName="degree" /></div>
                        <div class="form-group"><label>Domaine</label><input formControlName="field" /></div>
                      </div>
                      <div class="form-row">
                        <div class="form-group"><label>Début</label><input formControlName="startDate" type="month" /></div>
                        <div class="form-group"><label>Fin</label><input formControlName="endDate" type="month" /></div>
                      </div>
                      <div class="form-group"><label>Description</label><textarea formControlName="description" rows="2"></textarea></div>
                      <button type="button" class="btn btn-secondary btn-sm" (click)="removeEducation(i)">Supprimer</button>
                    </div>
                  }
                </div>
                <button type="button" class="btn btn-secondary" (click)="addEducation()">Ajouter une formation</button>
              </form>
        </div>
        <div class="wizard-step" [style.display]="isStepVisible('skills') ? 'block' : 'none'">
              <h2>Compétences</h2>
              <form [formGroup]="skillsForm">
                <div formArrayName="items">
                  @for (group of skillControls; track $index; let i = $index) {
                    <div class="list-item list-item-inline" [formGroupName]="i">
                      <div class="form-group"><label>Compétence</label><input formControlName="name" /></div>
                      <div class="form-group">
                        <label>Niveau</label>
                        <select formControlName="level">
                          <option value="Beginner">Débutant</option>
                          <option value="Intermediate">Intermédiaire</option>
                          <option value="Advanced">Avancé</option>
                          <option value="Expert">Expert</option>
                        </select>
                      </div>
                      <div class="form-group"><label>Catégorie</label><input formControlName="category" placeholder="Frontend, BTP…" /></div>
                      <button type="button" class="btn btn-secondary btn-sm" (click)="removeSkill(i)">Supprimer</button>
                    </div>
                  }
                </div>
                <button type="button" class="btn btn-secondary" (click)="addSkill()">Ajouter une compétence</button>
              </form>
        </div>
        <div class="wizard-step" [style.display]="isStepVisible('languages') ? 'block' : 'none'">
              <h2>Langues</h2>
              <form [formGroup]="languagesForm">
                <div formArrayName="items">
                  @for (group of languageControls; track $index; let i = $index) {
                    <div class="list-item list-item-inline" [formGroupName]="i">
                      <div class="form-group"><label>Langue</label><input formControlName="name" /></div>
                      <div class="form-group">
                        <label>Niveau</label>
                        <select formControlName="proficiency">
                          <option value="Basic">Notions</option>
                          <option value="Conversational">Conversationnel</option>
                          <option value="Fluent">Courant</option>
                          <option value="Native">Langue maternelle</option>
                        </select>
                      </div>
                      <button type="button" class="btn btn-secondary btn-sm" (click)="removeLanguage(i)">Supprimer</button>
                    </div>
                  }
                </div>
                <button type="button" class="btn btn-secondary" (click)="addLanguage()">Ajouter une langue</button>
              </form>
        </div>
        <div class="wizard-step" [style.display]="isStepVisible('certifications') ? 'block' : 'none'">
              <h2>Certifications</h2>
              <form [formGroup]="certificationsForm">
                <div formArrayName="items">
                  @for (group of certificationControls; track $index; let i = $index) {
                    <div class="list-item" [formGroupName]="i">
                      <div class="form-group">
                        <label>Type</label>
                        <select formControlName="certificationType">
                          <option value="PROFESSIONAL">Professionnelle</option>
                          <option value="REGULATORY">Réglementaire (CACES, SST…)</option>
                        </select>
                      </div>
                      <div class="form-group"><label>Nom</label><input formControlName="name" /></div>
                      <div class="form-row">
                        <div class="form-group"><label>Organisme</label><input formControlName="issuer" /></div>
                        <div class="form-group"><label>Date</label><input formControlName="date" type="month" /></div>
                      </div>
                      <div class="form-group"><label>URL</label><input formControlName="url" /></div>
                      <button type="button" class="btn btn-secondary btn-sm" (click)="removeCertification(i)">Supprimer</button>
                    </div>
                  }
                </div>
                <button type="button" class="btn btn-secondary" (click)="addCertification()">Ajouter une certification</button>
              </form>
        </div>
        <div class="wizard-step" [style.display]="isStepVisible('summary') ? 'block' : 'none'">
              <h2>Récapitulatif</h2>
              <p class="text-muted mb-2">Vérifiez vos informations avant enregistrement. Modifiez une section via le menu ou les liens ci-dessous.</p>

              <div class="summary-block">
                <div class="summary-header">
                  <h3>Profil</h3>
                  <button type="button" class="btn-link" (click)="goTo('profile')">Modifier</button>
                </div>
                <p><strong>{{ profileForm.controls.firstName.value }} {{ profileForm.controls.lastName.value }}</strong></p>
                <p class="text-muted">{{ profileForm.controls.title.value || '—' }}</p>
                <p class="text-muted">{{ profileForm.controls.email.value }} · {{ profileForm.controls.phone.value || 'tél. non renseigné' }}</p>
              </div>

              <div class="summary-block">
                <div class="summary-header">
                  <h3>Expériences</h3>
                  <button type="button" class="btn-link" (click)="goTo('experiences')">Modifier</button>
                </div>
                <p>{{ filledExperienceCount }} entrée(s)</p>
              </div>

              <div class="summary-block">
                <div class="summary-header">
                  <h3>Projets</h3>
                  <button type="button" class="btn-link" (click)="goTo('projects')">Modifier</button>
                </div>
                <p>{{ filledProjectCount }} entrée(s)</p>
              </div>

              <div class="summary-block">
                <div class="summary-header">
                  <h3>Formation</h3>
                  <button type="button" class="btn-link" (click)="goTo('education')">Modifier</button>
                </div>
                <p>{{ filledEducationCount }} entrée(s)</p>
              </div>

              <div class="summary-block">
                <div class="summary-header">
                  <h3>Compétences</h3>
                  <button type="button" class="btn-link" (click)="goTo('skills')">Modifier</button>
                </div>
                <p>{{ filledSkillCount }} entrée(s)</p>
              </div>

              <div class="summary-block">
                <div class="summary-header">
                  <h3>Langues</h3>
                  <button type="button" class="btn-link" (click)="goTo('languages')">Modifier</button>
                </div>
                <p>{{ filledLanguageCount }} entrée(s)</p>
              </div>

              <div class="summary-block">
                <div class="summary-header">
                  <h3>Certifications</h3>
                  <button type="button" class="btn-link" (click)="goTo('certifications')">Modifier</button>
                </div>
                <p>{{ filledCertificationCount }} entrée(s)</p>
              </div>
        </div>
        <div class="wizard-step" [style.display]="isStepVisible('publish') ? 'block' : 'none'">
              <h2>Publication</h2>
              @if (publishStatus) {
                <p>Statut : <strong>{{ publishStatus.published ? 'Publié' : 'Brouillon' }}</strong></p>
                @if (publishStatus.previewUrl) {
                  <button type="button" class="btn btn-secondary" (click)="openPreview()">Voir l'aperçu</button>
                }
                <div class="actions">
                  @if (!publishStatus.published) {
                    <button type="button" class="btn btn-primary" (click)="doPublish()" [disabled]="saving">Publier</button>
                  } @else {
                    <button type="button" class="btn btn-secondary" (click)="doUnpublish()" [disabled]="saving">Dépublier</button>
                  }
                </div>
              }
        </div>

        <div class="wizard-actions">
          @if (currentStepIndex > 0) {
            <button type="button" class="btn btn-secondary" (click)="previousStep()">Précédent</button>
          } @else {
            <a routerLink="/dashboard" class="btn btn-secondary">Tableau de bord</a>
          }

          @if (currentStep === 'summary') {
            <button type="button" class="btn btn-primary" (click)="saveAll()" [disabled]="saving">
              @if (saving) { Enregistrement… } @else { Enregistrer le portfolio }
            </button>
          } @else if (currentStep !== 'publish') {
            <button type="button" class="btn btn-primary" (click)="nextStep()" [disabled]="saving">Suivant</button>
          } @else {
            <a routerLink="/dashboard" class="btn btn-primary">Retour au tableau de bord</a>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .wizard-layout { display: grid; grid-template-columns: 220px 1fr; gap: 1rem; margin: 1.5rem auto; align-items: start; }
    @media (max-width: 768px) { .wizard-layout { grid-template-columns: 1fr; } }
    .step-nav { display: flex; flex-direction: column; gap: 0.35rem; margin: 1rem 0; }
    .step-item { text-align: left; background: none; border: none; padding: 0.45rem 0.5rem; border-radius: 6px; cursor: pointer; font: inherit; }
    .step-item.active { background: #eff6ff; color: var(--color-primary); font-weight: 600; }
    .step-num { display: inline-block; width: 1.25rem; opacity: 0.7; }
    .back-link { font-size: 0.875rem; }
    .list-item { border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 1rem; margin-bottom: 0.75rem; }
    .list-item-inline { display: grid; grid-template-columns: 1fr 1fr 1fr auto; gap: 0.75rem; align-items: end; }
    @media (max-width: 768px) { .list-item-inline { grid-template-columns: 1fr; } }
    .actions, .wizard-actions { display: flex; gap: 0.5rem; margin-top: 1rem; flex-wrap: wrap; }
    .wizard-actions { padding-top: 1rem; border-top: 1px solid var(--color-border); justify-content: space-between; }
    .btn-sm { padding: 0.35rem 0.65rem; font-size: 0.85rem; }
    .mb-2 { margin-bottom: 0.75rem; }
    textarea { width: 100%; padding: 0.6rem 0.75rem; border: 1px solid var(--color-border); border-radius: var(--radius-md); font: inherit; resize: vertical; }
    .alert-success { background: #ecfdf5; color: #065f46; border: 1px solid #a7f3d0; }
    .summary-block { border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 1rem; margin-bottom: 0.75rem; }
    .summary-header { display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; margin-bottom: 0.35rem; }
    .summary-header h3 { margin: 0; font-size: 1rem; }
    .summary-block p { margin: 0.2rem 0; }
    .btn-link { background: none; border: none; padding: 0; color: var(--color-primary); cursor: pointer; font: inherit; text-decoration: underline; }
    .portfolio-loading { margin-bottom: 0.75rem; font-size: 0.875rem; }
  `],
})
export class WizardComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly portfolio = inject(PortfolioService);
  private readonly auth = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly steps = WIZARD_STEPS;
  readonly userTypeOptions = USER_TYPE_OPTIONS;
  currentStep: WizardStep = 'profile';
  portfolioLoading = false;
  saving = false;
  message = '';
  error = '';
  publishStatus: PublishStatus | null = null;

  profileForm = this.fb.group({
    userType: ['CANDIDATE'],
    firstName: [''],
    lastName: [''],
    email: [''],
    title: [''],
    summary: [''],
    phone: [''],
    location: [''],
    website: [''],
    linkedin: [''],
    github: [''],
    primaryTrade: [''],
    tradeSpecialties: [''],
    studentInstitution: [''],
    studentYear: [''],
    internshipSought: [''],
  });

  experiencesForm = this.fb.group({ items: this.fb.array<FormGroup>([]) });
  projectsForm = this.fb.group({ items: this.fb.array<FormGroup>([]) });
  educationForm = this.fb.group({ items: this.fb.array<FormGroup>([]) });
  skillsForm = this.fb.group({ items: this.fb.array<FormGroup>([]) });
  languagesForm = this.fb.group({ items: this.fb.array<FormGroup>([]) });
  certificationsForm = this.fb.group({ items: this.fb.array<FormGroup>([]) });

  get currentStepIndex(): number {
    return this.steps.findIndex((s) => s.key === this.currentStep);
  }

  get experienceControls(): FormGroup[] {
    return this.experiencesForm.controls.items.controls;
  }

  get projectControls(): FormGroup[] {
    return this.projectsForm.controls.items.controls;
  }

  get educationControls(): FormGroup[] {
    return this.educationForm.controls.items.controls;
  }

  get skillControls(): FormGroup[] {
    return this.skillsForm.controls.items.controls;
  }

  get languageControls(): FormGroup[] {
    return this.languagesForm.controls.items.controls;
  }

  get certificationControls(): FormGroup[] {
    return this.certificationsForm.controls.items.controls;
  }

  get filledExperienceCount(): number {
    return this.filteredExperiences().length;
  }

  get filledProjectCount(): number {
    return this.filteredProjects().length;
  }

  get filledEducationCount(): number {
    return this.filteredEducation().length;
  }

  get filledSkillCount(): number {
    return this.filteredSkills().length;
  }

  get filledLanguageCount(): number {
    return this.filteredLanguages().length;
  }

  get filledCertificationCount(): number {
    return this.filteredCertifications().length;
  }

  ngOnInit(): void {
    this.applyProfile({});
    this.portfolioLoading = true;

    this.portfolio.getPortfolio().subscribe({
      next: (portfolio) => {
        this.portfolioLoading = false;
        this.applyProfile(portfolio.profile ?? {});
        this.setFormArray(this.experiencesForm.controls.items, portfolio.experiences ?? [], newExperienceGroup);
        this.setFormArray(this.projectsForm.controls.items, portfolio.projects ?? [], newProjectGroup);
        this.setFormArray(this.educationForm.controls.items, portfolio.education ?? [], newEducationGroup);
        this.setFormArray(this.skillsForm.controls.items, portfolio.skills ?? [], newSkillGroup);
        this.setFormArray(this.languagesForm.controls.items, portfolio.languages ?? [], newLanguageGroup);
        this.setFormArray(this.certificationsForm.controls.items, portfolio.certifications ?? [], newCertificationGroup);
        this.cdr.detectChanges();
        this.loadPublishStatus();
      },
      error: (err: HttpErrorResponse) => {
        this.portfolioLoading = false;
        this.error = this.loadErrorMessage(err);
        this.cdr.detectChanges();
      },
    });
  }

  isStepVisible(step: WizardStep): boolean {
    return this.currentStep === step;
  }

  goTo(step: WizardStep): void {
    this.currentStep = step;
    this.clearMessages();
    if (step === 'publish') {
      this.loadPublishStatus();
    }
    this.cdr.detectChanges();
  }

  previousStep(): void {
    const prev = this.currentStepIndex - 1;
    if (prev >= 0) {
      this.goTo(this.steps[prev].key);
    }
  }

  nextStep(): void {
    this.clearMessages();
    const next = this.currentStepIndex + 1;
    if (next < this.steps.length) {
      this.goTo(this.steps[next].key);
    }
  }

  saveAll(): void {
    this.clearMessages();
    if (!this.validateFullProfile()) {
      this.goTo('profile');
      return;
    }

    this.saving = true;
    forkJoin({
      profile: this.portfolio.saveProfile(this.profileForm.getRawValue() as UserProfile),
      experiences: this.portfolio.saveExperiences(this.filteredExperiences()),
      projects: this.portfolio.saveProjects(this.filteredProjects()),
      education: this.portfolio.saveEducation(this.filteredEducation()),
      skills: this.portfolio.saveSkills(this.filteredSkills()),
      languages: this.portfolio.saveLanguages(this.filteredLanguages()),
      certifications: this.portfolio.saveCertifications(this.filteredCertifications()),
    }).subscribe({
      next: ({ profile, experiences, projects, education, skills, languages, certifications }) => {
        this.applyProfile(profile);
        this.setFormArray(this.experiencesForm.controls.items, experiences, newExperienceGroup);
        this.setFormArray(this.projectsForm.controls.items, projects, newProjectGroup);
        this.setFormArray(this.educationForm.controls.items, education, newEducationGroup);
        this.setFormArray(this.skillsForm.controls.items, skills, newSkillGroup);
        this.setFormArray(this.languagesForm.controls.items, languages, newLanguageGroup);
        this.setFormArray(this.certificationsForm.controls.items, certifications, newCertificationGroup);
        this.message = 'Portfolio enregistré avec succès.';
        this.saving = false;
        this.goTo('publish');
      },
      error: () => {
        this.error = 'Erreur lors de l\'enregistrement.';
        this.saving = false;
      },
    });
  }

  addExperience(): void { this.experiencesForm.controls.items.push(newExperienceGroup(this.fb)); }
  removeExperience(i: number): void { this.experiencesForm.controls.items.removeAt(i); }

  addProject(): void { this.projectsForm.controls.items.push(newProjectGroup(this.fb)); }
  removeProject(i: number): void { this.projectsForm.controls.items.removeAt(i); }

  addEducation(): void { this.educationForm.controls.items.push(newEducationGroup(this.fb)); }
  removeEducation(i: number): void { this.educationForm.controls.items.removeAt(i); }

  addSkill(): void { this.skillsForm.controls.items.push(newSkillGroup(this.fb)); }
  removeSkill(i: number): void { this.skillsForm.controls.items.removeAt(i); }

  addLanguage(): void { this.languagesForm.controls.items.push(newLanguageGroup(this.fb)); }
  removeLanguage(i: number): void { this.languagesForm.controls.items.removeAt(i); }

  addCertification(): void { this.certificationsForm.controls.items.push(newCertificationGroup(this.fb)); }
  removeCertification(i: number): void { this.certificationsForm.controls.items.removeAt(i); }

  doPublish(): void {
    this.runSave(this.portfolio.publish(), (s) => { this.publishStatus = s; });
  }

  doUnpublish(): void {
    this.runSave(this.portfolio.unpublish(), (s) => { this.publishStatus = s; });
  }

  onPhoneInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '').slice(0, 8);
    this.profileForm.controls.phone.setValue(digits, { emitEvent: false });
    input.value = digits;
  }

  openPreview(): void {
    this.portfolio.getPreviewHtml().subscribe({
      next: (html) => {
        const tab = window.open();
        if (tab) {
          tab.document.open();
          tab.document.write(html);
          tab.document.close();
        }
      },
      error: () => {
        this.error = 'Impossible d\'afficher l\'aperçu.';
      },
    });
  }

  private applyProfile(profile: Partial<UserProfile>): void {
    const phone = this.normalizePhone(profile.phone);
    this.profileForm.patchValue({ ...profile, phone });
    const stored = this.auth.getStoredUser();
    if (!stored) {
      return;
    }
    const current = this.profileForm.getRawValue();
    this.profileForm.patchValue({
      firstName: current.firstName || stored.firstName,
      lastName: current.lastName || stored.lastName,
      email: current.email || stored.email,
      userType: current.userType || stored.userType,
    });
  }

  private normalizePhone(value: string | undefined): string {
    const digits = (value ?? '').replace(/\D/g, '');
    return digits.length >= 8 ? digits.slice(-8) : digits;
  }

  private validateFullProfile(): boolean {
    this.applyProfileSaveValidators();
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.error = 'Profil incomplet : corrigez les champs en rouge (étape Profil).';
      return false;
    }
    return true;
  }

  showProfileError(field: ProfileField): boolean {
    const control = this.profileForm.controls[field];
    return control.touched && control.invalid;
  }

  profileErrorMessage(field: ProfileField): string {
    const control = this.profileForm.controls[field];
    if (control.hasError('required')) {
      return `${PROFILE_FIELD_LABELS[field]} requis`;
    }
    if (field === 'email' && control.hasError('email')) {
      return 'Email valide requis';
    }
    if (field === 'phone' && control.hasError('pattern')) {
      return 'Le téléphone doit contenir exactement 8 chiffres';
    }
    return 'Valeur invalide';
  }

  private applyProfileSaveValidators(): void {
    const userType = this.profileForm.controls.userType.value ?? 'CANDIDATE';
    this.profileForm.controls.firstName.setValidators(Validators.required);
    this.profileForm.controls.lastName.setValidators(Validators.required);
    this.profileForm.controls.email.setValidators([Validators.required, Validators.email]);
    this.profileForm.controls.title.setValidators(Validators.required);
    this.profileForm.controls.phone.setValidators([Validators.required, Validators.pattern(PHONE_PATTERN)]);

    if (userType === 'TRADES_WORKER') {
      this.profileForm.controls.primaryTrade.setValidators(Validators.required);
      this.profileForm.controls.studentInstitution.clearValidators();
    } else if (userType === 'STUDENT') {
      this.profileForm.controls.studentInstitution.setValidators(Validators.required);
      this.profileForm.controls.primaryTrade.clearValidators();
    } else {
      this.profileForm.controls.primaryTrade.clearValidators();
      this.profileForm.controls.studentInstitution.clearValidators();
    }

    this.profileForm.updateValueAndValidity();
  }

  private loadPublishStatus(): void {
    this.portfolio.getPublishStatus().subscribe({
      next: (status) => { this.publishStatus = status; },
      error: (err: HttpErrorResponse) => {
        if (!this.error) {
          this.error = this.loadErrorMessage(err);
        }
      },
    });
  }

  private loadErrorMessage(err: HttpErrorResponse): string {
    if (err.status === 404) {
      return 'API portfolio introuvable (404). Arrêtez le backend, lancez backend\\compile-and-run.bat, puis vérifiez http://localhost:8080/api/health → "portfolio": true.';
    }
    if (err.status === 401) {
      return 'Session expirée. Reconnectez-vous.';
    }
    return 'Impossible de charger le profil.';
  }

  private filteredExperiences(): Experience[] {
    return this.experienceControls.map((g) => g.getRawValue() as Experience).filter((e) => e.company?.trim());
  }

  private filteredProjects(): Project[] {
    return this.projectControls.map((g) => g.getRawValue() as Project).filter((p) => p.name?.trim());
  }

  private filteredEducation(): Education[] {
    return this.educationControls.map((g) => g.getRawValue() as Education).filter((e) => e.institution?.trim());
  }

  private filteredSkills(): Skill[] {
    return this.skillControls.map((g) => g.getRawValue() as Skill).filter((s) => s.name?.trim());
  }

  private filteredLanguages(): Language[] {
    return this.languageControls.map((g) => g.getRawValue() as Language).filter((l) => l.name?.trim());
  }

  private filteredCertifications(): Certification[] {
    return this.certificationControls.map((g) => g.getRawValue() as Certification).filter((c) => c.name?.trim());
  }

  private setFormArray<T>(
    array: FormArray<FormGroup>,
    items: T[],
    factory: (fb: FormBuilder, item?: T) => FormGroup,
  ): void {
    array.clear();
    for (const item of items) {
      array.push(factory(this.fb, item));
    }
  }

  private clearMessages(): void {
    this.message = '';
    this.error = '';
  }

  private runSave<T>(call: import('rxjs').Observable<T>, onSuccess: (value: T) => void): void {
    this.saving = true;
    this.error = '';
    call.subscribe({
      next: (value) => {
        onSuccess(value);
        this.message = 'Enregistré.';
        this.saving = false;
      },
      error: () => {
        this.error = 'Erreur lors de l\'enregistrement.';
        this.saving = false;
      },
    });
  }
}

function newExperienceGroup(fb: FormBuilder, item: Experience = emptyExperience()): FormGroup {
  return fb.group({
    id: [item.id ?? null],
    company: [item.company ?? ''],
    position: [item.position ?? ''],
    location: [item.location ?? ''],
    startDate: [item.startDate ?? ''],
    endDate: [item.endDate ?? ''],
    current: [item.current ?? false],
    description: [item.description ?? ''],
  });
}

function newProjectGroup(fb: FormBuilder, item: Project = emptyProject()): FormGroup {
  return fb.group({
    id: [item.id ?? null],
    name: [item.name ?? ''],
    description: [item.description ?? ''],
    url: [item.url ?? ''],
    technologies: [item.technologies ?? ''],
    startDate: [item.startDate ?? ''],
    endDate: [item.endDate ?? ''],
  });
}

function newEducationGroup(fb: FormBuilder, item: Education = emptyEducation()): FormGroup {
  return fb.group({
    id: [item.id ?? null],
    institution: [item.institution ?? ''],
    degree: [item.degree ?? ''],
    field: [item.field ?? ''],
    startDate: [item.startDate ?? ''],
    endDate: [item.endDate ?? ''],
    description: [item.description ?? ''],
    educationType: [item.educationType ?? 'ACADEMIC'],
  });
}

function newSkillGroup(fb: FormBuilder, item: Skill = emptySkill()): FormGroup {
  return fb.group({
    id: [item.id ?? null],
    name: [item.name ?? ''],
    level: [item.level ?? 'Intermediate'],
    category: [item.category ?? ''],
  });
}

function newLanguageGroup(fb: FormBuilder, item: Language = emptyLanguage()): FormGroup {
  return fb.group({
    id: [item.id ?? null],
    name: [item.name ?? ''],
    proficiency: [item.proficiency ?? 'Conversational'],
  });
}

function newCertificationGroup(fb: FormBuilder, item: Certification = emptyCertification()): FormGroup {
  return fb.group({
    id: [item.id ?? null],
    name: [item.name ?? ''],
    issuer: [item.issuer ?? ''],
    date: [item.date ?? ''],
    url: [item.url ?? ''],
    certificationType: [item.certificationType ?? 'PROFESSIONAL'],
  });
}

function emptyExperience(): Experience {
  return { company: '', position: '', location: '', startDate: '', endDate: '', current: false, description: '' };
}

function emptyProject(): Project {
  return { name: '', description: '', url: '', technologies: '', startDate: '', endDate: '' };
}

function emptyEducation(): Education {
  return { institution: '', degree: '', field: '', startDate: '', endDate: '', description: '', educationType: 'ACADEMIC' };
}

function emptySkill(): Skill {
  return { name: '', level: 'Intermediate', category: '' };
}

function emptyLanguage(): Language {
  return { name: '', proficiency: 'Conversational' };
}

function emptyCertification(): Certification {
  return { name: '', issuer: '', date: '', url: '', certificationType: 'PROFESSIONAL' };
}
