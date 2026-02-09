import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatMessage } from '../../services/chat.service';
import { MarkdownModule } from 'ngx-markdown';

@Component({
  selector: 'app-chat-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownModule],
  templateUrl: './chat-dialog.component.html',
  styleUrls: ['./chat-dialog.component.scss']
})
export class ChatDialogComponent {
  messages: ChatMessage[] = [];
  newMessage: string = '';
  isOpen: boolean = false;
  isExpanded: boolean = false;
  isLoading: boolean = false;

  constructor(private chatService: ChatService) {}

  toggleChat() {
    this.isOpen = !this.isOpen;
    if (!this.isOpen) {
      this.isExpanded = false;
    }
  }

  toggleExpand(event: Event) {
    event.stopPropagation();
    this.isExpanded = !this.isExpanded;
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;

    const userMsg: ChatMessage = { role: 'user', content: this.newMessage };
    this.messages.push(userMsg);
    const messageToSend = this.newMessage;
    this.newMessage = '';
    this.isLoading = true;

    this.chatService.sendMessage(messageToSend).subscribe({
      next: (response) => {
        this.messages.push({ role: 'assistant', content: response });
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error sending message', error);
        this.messages.push({ role: 'assistant', content: 'Desculpe, ocorreu um erro.' });
        this.isLoading = false;
      }
    });
  }
}
