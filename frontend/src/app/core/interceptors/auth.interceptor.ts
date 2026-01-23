import {HttpInterceptorFn} from '@angular/common/http';
import {AuthService} from '../../services/auth.service';
import { inject } from '@angular/core';
import {from, switchMap} from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  //só adiciona o token se for chamada pra api
  if(req.url.includes('/api')){

    //converte Promise do supabase em observable
    return from(authService.getSessionToken()).pipe(
      switchMap((token) =>{
        if(token){

          //clona a requisicao e adiciona no header o auth
          const cloned = req.clone({
            setHeaders: {
              Authorization: `Bearer ${token}`
            }
          });
          return next(cloned);
        }
        return next(req);
      })
    );
  }
  return next(req);
}
