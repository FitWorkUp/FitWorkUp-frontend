# Imagens das conquistas

O Android não permite criar subpastas dentro de `res/drawable`. Para manter os
arquivos organizados, use `app/src/main/res/drawable-nodpi` e o prefixo
`achievement_` como namespace dos ícones.

Exemplos:

- `achievement_c01.png`
- `achievement_c02.png`
- `achievement_c30.webp`

Recomendações:

- imagem quadrada, preferencialmente 512 x 512 pixels;
- PNG ou WebP com fundo transparente;
- nome somente com letras minúsculas, números e sublinhado;
- não alterar a chave enviada pela API, salvo quando quiser substituir o ícone.

Enquanto a imagem correspondente não existir, o aplicativo apresenta o troféu
padrão. A pasta `drawable-nodpi` evita que o Android redimensione o arquivo com
base na densidade da tela.
