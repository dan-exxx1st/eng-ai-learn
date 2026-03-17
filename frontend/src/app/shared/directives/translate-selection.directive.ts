import { Directive, ElementRef, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Directive({
  selector: '[appTranslateSelection]',
  standalone: true
})
export class TranslateSelectionDirective implements OnInit, OnDestroy {
  private tooltip: HTMLDivElement | null = null;
  private mouseUpListener: (() => void) | null = null;
  private clickOutsideListener: (() => void) | null = null;

  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.mouseUpListener = this.renderer.listen(this.el.nativeElement, 'mouseup', (event: MouseEvent) => {
      setTimeout(() => this.handleSelection(event), 10);
    });
    this.clickOutsideListener = this.renderer.listen('document', 'mousedown', (event: MouseEvent) => {
      if (this.tooltip && !this.tooltip.contains(event.target as Node)) {
        this.removeTooltip();
      }
    });
  }

  ngOnDestroy() {
    this.mouseUpListener?.();
    this.clickOutsideListener?.();
    this.removeTooltip();
  }

  private handleSelection(event: MouseEvent) {
    const selection = window.getSelection();
    const text = selection?.toString().trim();

    if (!text || text.length < 2 || text.length > 100) {
      return;
    }

    this.removeTooltip();
    this.showTooltip(event.clientX, event.clientY, text);
  }

  private showTooltip(x: number, y: number, text: string) {
    this.tooltip = this.renderer.createElement('div');
    this.tooltip!.className = 'translate-tooltip';
    this.tooltip!.innerHTML = `<span class="translate-loading">Translating...</span>`;

    Object.assign(this.tooltip!.style, {
      position: 'fixed',
      left: `${x}px`,
      top: `${y + 10}px`,
      background: '#333',
      color: '#fff',
      padding: '8px 12px',
      borderRadius: '6px',
      fontSize: '14px',
      zIndex: '10000',
      maxWidth: '300px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
      pointerEvents: 'auto',
      lineHeight: '1.4'
    });

    document.body.appendChild(this.tooltip!);

    // Adjust position if tooltip goes off-screen
    const rect = this.tooltip!.getBoundingClientRect();
    if (rect.right > window.innerWidth) {
      this.tooltip!.style.left = `${window.innerWidth - rect.width - 10}px`;
    }
    if (rect.bottom > window.innerHeight) {
      this.tooltip!.style.top = `${y - rect.height - 10}px`;
    }

    this.http.post<any>('/api/vocabulary/translate', { text }).subscribe({
      next: (res) => {
        if (this.tooltip) {
          this.tooltip.innerHTML = `
            <div style="font-weight:bold;margin-bottom:4px">${this.escapeHtml(text)}</div>
            <div style="color:#90caf9">${this.escapeHtml(res.translation)}</div>
          `;
        }
      },
      error: () => {
        if (this.tooltip) {
          this.tooltip.innerHTML = `<span style="color:#ff8a80">Translation failed</span>`;
        }
      }
    });
  }

  private removeTooltip() {
    if (this.tooltip) {
      this.tooltip.remove();
      this.tooltip = null;
    }
  }

  private escapeHtml(str: string): string {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }
}
