import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputNumberModule } from 'primeng/inputnumber';
import { CheckboxModule } from 'primeng/checkbox';
import { CardModule } from 'primeng/card';

// Services
import { UserService, UserProfile } from '../../services/user.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    InputNumberModule,
    CheckboxModule,
    CardModule
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile implements OnInit {

  userProfile: UserProfile = {
    fullName: '',
    email: '',
    grossSalary: 0,
    netSalaryEstimate: 0,
    receivesPlr: false,
    plrEstimate: 0,
    savingsGoal: 0
  };

  saving = false;
  loading = true;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.loading = true;
    this.userService.getProfile().subscribe({
      next: (data) => {
        this.userProfile = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erro ao carregar perfil', err);
        this.loading = false;
      }
    });
  }

  saveProfile() {
    this.saving = true;
    this.userService.updateProfile(this.userProfile).subscribe({
      next: (updated) => {
        this.userProfile = updated;
        this.saving = false;
        alert('Perfil atualizado com sucesso!');
      },
      error: (err) => {
        console.error('Erro ao salvar perfil', err);
        this.saving = false;
        alert('Erro ao atualizar perfil.');
      }
    });
  }
}
