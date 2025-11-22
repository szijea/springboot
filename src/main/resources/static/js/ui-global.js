// ui-global.js - 全局统一交互增强 (滚动进度 / 主题切换 / 键盘导航 / 无障碍提示)
(function(){
  const doc = document;
  function initScrollProgress(){
    const bar = doc.getElementById('scroll-progress');
    if(!bar) return;
    bar.classList.add('enhanced-bar');
    function update(){
      const h = doc.documentElement.scrollHeight - window.innerHeight;
      const sc = window.scrollY; bar.style.width = h>0? (sc/h)*100 + '%' : '0%';
    }
    window.addEventListener('scroll', update, {passive:true});
    update();
  }
  function initThemeToggle(){
    if(doc.getElementById('theme-toggle')) return;
    const btn = doc.createElement('button');
    btn.id='theme-toggle'; btn.className='theme-toggle header-icon-btn'; btn.type='button';
    btn.setAttribute('aria-label','切换主题');
    btn.innerHTML='<i class="fa fa-moon-o"></i>';
    btn.onclick=()=>{ const dark=doc.documentElement.classList.toggle('dark'); btn.innerHTML='<i class="fa '+(dark?'fa-sun-o':'fa-moon-o')+'"></i>'; announce && announce(dark?'已切换到暗色模式':'已切换到浅色模式'); };
    doc.body.appendChild(btn);
  }
  function initSidebarKeyboard(){
    const items = Array.from(doc.querySelectorAll('aside .sidebar-item'));
    if(!items.length) return;
    items.forEach(it=>{ it.setAttribute('tabindex','0'); });
    doc.addEventListener('keydown',e=>{
      if(['ArrowDown','ArrowUp'].includes(e.key)){
        const activeEl = doc.activeElement;
        if(!items.includes(activeEl)) return;
        e.preventDefault();
        let idx = items.indexOf(activeEl);
        idx = e.key==='ArrowDown'? (idx+1)%items.length : (idx-1+items.length)%items.length;
        items[idx].focus();
      }
      if(e.key==='Enter' && items.includes(doc.activeElement)){
        doc.activeElement.click();
      }
    });
  }
  function markAriaCurrent(){
    const active = doc.querySelector('aside .sidebar-item.active');
    if(active) active.setAttribute('aria-current','page');
  }
  function initRipple(){
    doc.querySelectorAll('.btn').forEach(btn=>btn.setAttribute('data-ripple',''));
  }
  function applyARIA(){
    const aside = doc.querySelector('aside'); if(aside){ aside.setAttribute('role','navigation'); aside.setAttribute('aria-label','侧边主导航'); }
    const header = doc.querySelector('header'); if(header){ header.setAttribute('role','banner'); }
  }
  function init(){ initScrollProgress(); initThemeToggle(); initSidebarKeyboard(); markAriaCurrent(); initRipple(); applyARIA(); }
  if(doc.readyState==='loading'){ doc.addEventListener('DOMContentLoaded', init); } else init();
})();

