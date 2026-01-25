import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router, RouterModule} from '@angular/router';
import {AuthService} from '../../services/auth.service';

//PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { FloatLabelModule } from 'primeng/floatlabel';
import { MessageModule } from 'primeng/message';
import {MessageService} from 'primeng/api';
import {ToastModule} from 'primeng/toast';



@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    FloatLabelModule,
    MessageModule,
    ToastModule,
    RouterModule

  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  providers:[MessageService],
  standalone: true
})
export class Login {

  email='';
  password='';
  loading = false;
  errorMessage = '';


  constructor(
    private authService: AuthService,
    private  router: Router,
    private messageService: MessageService) {}

  async onLogin(){

    if (!this.email || !this.password) {
      this.messageService.add({ severity: 'warn', summary: 'Atenção', detail: 'Preencha todos os campos.' });
      return;
    }
    this.loading = true;

    try {
      const {data, error} = await this.authService.signIn(this.email,this.password);

      if(error){
        this.messageService.add({
          severity: 'error',
          summary:'Erro.',
          detail: error.message || "Falha na autenticação"
        });
        this.loading = false;
        return;
      }
      this.messageService.add({
        severity: 'success',
        summary: 'Bem-vindo!',
        detail: 'Login realizado com sucesso.'
      });
      this.router.navigate(['/dashboard']);

    }catch (err: any){
      console.error(err);
      this.messageService.add({
        severity: 'error',
        summary: 'Erro Crítico',
        detail: 'Ocorreu um erro inesperado.'
      });
    }finally {
      this.loading = false;
    }
  }
}
