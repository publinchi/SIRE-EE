import { Routes, RouterModule } from '@angular/router';
import { ModuleWithProviders } from '@angular/core';
import { DashboardDemoComponent } from './demo/view/dashboarddemo.component';
import { SampleDemoComponent } from './demo/view/sampledemo.component';
import { FormsDemoComponent } from './demo/view/formsdemo.component';
import { DataDemoComponent } from './demo/view/datademo.component';
import { PanelsDemoComponent } from './demo/view/panelsdemo.component';
import { OverlaysDemoComponent } from './demo/view/overlaysdemo.component';
import { MenusDemoComponent } from './demo/view/menusdemo.component';
import { MessagesDemoComponent } from './demo/view/messagesdemo.component';
import { MiscDemoComponent } from './demo/view/miscdemo.component';
import { EmptyDemoComponent } from './demo/view/emptydemo.component';
import { ChartsDemoComponent } from './demo/view/chartsdemo.component';
import { FileDemoComponent } from './demo/view/filedemo.component';
import { DocumentationComponent } from './demo/view/documentation.component';
import { LoginComponent } from './login';
import { RegisterComponent } from './register';
import { AuthGuard } from './_guards';

export const routes: Routes = [
    { path: '', component: DashboardDemoComponent, canActivate: [AuthGuard] },
    { path: 'sample', component: SampleDemoComponent, canActivate: [AuthGuard] },
    { path: 'forms', component: FormsDemoComponent, canActivate: [AuthGuard] },
    { path: 'data', component: DataDemoComponent, canActivate: [AuthGuard] },
    { path: 'panels', component: PanelsDemoComponent, canActivate: [AuthGuard] },
    { path: 'overlays', component: OverlaysDemoComponent, canActivate: [AuthGuard] },
    { path: 'menus', component: MenusDemoComponent, canActivate: [AuthGuard] },
    { path: 'messages', component: MessagesDemoComponent, canActivate: [AuthGuard] },
    { path: 'misc', component: MiscDemoComponent, canActivate: [AuthGuard] },
    { path: 'empty', component: EmptyDemoComponent, canActivate: [AuthGuard] },
    { path: 'charts', component: ChartsDemoComponent, canActivate: [AuthGuard] },
    { path: 'file', component: FileDemoComponent, canActivate: [AuthGuard] },
    { path: 'documentation', component: DocumentationComponent, canActivate: [AuthGuard] },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },

    // otherwise redirect to home
    { path: '**', redirectTo: '' }
];

export const AppRoutes: ModuleWithProviders = RouterModule.forRoot(routes);
