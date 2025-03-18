// Загружаем сайдбар с помощью HTMX
htmx.ajax('GET', '/views/sidebar.html', {
    target: document.body,
    swap: 'afterbegin',
});

// Функция для открытия/закрытия сайдбара
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    sidebar.classList.toggle('active');
}