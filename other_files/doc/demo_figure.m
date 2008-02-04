
set(0, 'DefaultTextInterpreter', 'none');

x = 0:0.1:10;
y = sin(2 * x) + sqrt(x);

fh = figure;
set(fh, 'Units', 'centimeters');
set(fh, 'Position', [5 5 8 6]);
set(fh, 'PaperPositionMode', 'auto');
idx = 41;
plot(x, y, x(idx),y(idx), '.');
text(x(41), y(41), 'eq', 'FontWeight', 'bold', 'FontSize', 8);
xlabel('xlabel');
ylabel('Amplitude (-)');
title('\tex[cc][cc]{This is the title $\sqrt{x^2}$}');

print('-depsc2', 'demo_figure.eps');
