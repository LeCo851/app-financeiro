import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {Router, RouterModule} from '@angular/router';
import {MessageService} from 'primeng/api';
import {ButtonModule} from 'primeng/button';
import {PasswordModule} from 'primeng/password';
import {InputTextModule} from 'primeng/inputtext';
import {ToastModule} from 'primeng/toast';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    PasswordModule,
    InputTextModule,
    ToastModule,
    RouterModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
  providers: [MessageService]
})
export class Register {

  registerForm: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', [Validators.required,Validators.minLength(3)]],
      email: ['',[Validators.required,Validators.email]],
      password: ['',[Validators.required,Validators.minLength(6)]]
    });
  }

  async onSubmit(){
    if (this.registerForm.invalid) return;

    this.loading = true;
    const{ fullName, email, password } = this.registerForm.value;

    try {
      const {data, error} = await this.authService.signUp(email, password, fullName);
      if(error) throw  error;

      this.messageService.add({
        severity: 'success',
        summary: 'Sucesso',
        detail: 'Conta criada! Verifique seu e-mail ou faça login.'
      });

      setTimeout(() =>{
        this.router.navigate(['/login']);
      },1500);

    }catch (error: any){
      console.error(error);
      this.messageService.add({
        severity:'error',
        summary: 'Erro',
        detail: error.message || "Falha ao criar conta"
      })
    }finally {
      this.loading = false;
    }
  }
}
