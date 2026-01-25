import { Injectable } from '@angular/core';
import {createClient,SupabaseClient,User, Session} from '@supabase/supabase-js';
import {environment} from '../../environment/environment';
import {BehaviorSubject,from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly supabase: SupabaseClient;

  private readonly userSubject = new BehaviorSubject<User | null>(null);
  //exposto como Observable para os componentes só olharem o valor
  public user$ = this.userSubject.asObservable(); // readOnly do observable

  constructor(private router: Router) {
    //init client com as chaves do environment
    this.supabase = createClient(environment.supabaseUrl,environment.supabaseKey);

    //recupera a sessao do user
    this.supabase.auth.getSession().then(({ data }) =>{
      this.userSubject.next(data.session?.user ?? null);
    });

    this.supabase.auth.onAuthStateChange((event, session) =>{
      if(event === 'SIGNED_IN' && session){
        this.userSubject.next(session.user);
      }else if (event === 'SIGNED_OUT'){
        this.userSubject.next(null);
        this.router.navigate(['/login']);
      }
    });
  }

  async signOut(){
    await this.supabase.auth.signOut();
    this.router.navigate(['/login']);
  }
  async getSessionToken(): Promise<String | null>{
    const { data } = await this.supabase.auth.getSession();
    return data.session?.access_token || null;
  }

  get isAuthenticated(): boolean {
    return !!this.userSubject.value;
  }

  async signUp(email:string, password: string, fullName: string){
    const{data, error} = await  this.supabase.auth.signUp({
      email: email,
      password: password,
      options: {
        data:{
          full_name: fullName,
        },
      },
    });
    return {data ,error};
  }

  async signIn(email: string, password: string){
    const {data, error} = await this.supabase.auth.signInWithPassword({
      email,
      password
    });
    return {data, error};
  }
}
