import { Component } from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';

//PrimeNG
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { FloatLabelModule } from 'primeng/floatlabel';
import { MessageModule } from 'primeng/message';



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
    MessageModule

  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  email='';
  password='';
  loading = false;
  errorMessage = '';

  constructor(private authService: AuthService, private  router: Router) {}

  onSubmit(){
    this.errorMessage = '';
    this.loading = true;

    this.authService.signIn(this.email, this.password).subscribe({
      next: (response) =>{
        this.loading = false;
        if (response.error){
          this.errorMessage = "Erro: " + response.error.message;
        }else {
          this.router.navigate(['/dashboard'])
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = "Erro de conexão.";
        console.error(err);
      }
    });
  }
}
