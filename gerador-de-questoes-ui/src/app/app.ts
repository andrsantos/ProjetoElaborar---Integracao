import { Component, signal } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { Sidebar } from './components/sidebar/sidebar';
import { LoadingBarComponent } from './components/loading-bar/loading-bar';

@Component({
  selector: 'app-root',
  standalone: true, 
  imports: [CommonModule, RouterOutlet, Sidebar, LoadingBarComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss' 
})
export class App {
  protected readonly title = signal('gerador-de-questoes-ui');
  
  protected readonly exibirLayoutPadrao = signal(true);

  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      
      const url = event.urlAfterRedirects || event.url;
      
      const isAuthPage = url.includes('/login') || url.includes('/registrar');
      
      this.exibirLayoutPadrao.set(!isAuthPage);
      
    });
  }
}