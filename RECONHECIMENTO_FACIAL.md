# Sistema de Reconhecimento Facial

Este documento explica como usar o sistema de reconhecimento facial implementado no projeto CPMA.

## Funcionalidades Implementadas

### 1. Detecção Facial em Tempo Real

- **Localização**: `CameraController.java`
- **Funcionalidade**: Detecta faces automaticamente quando a câmera é aberta
- **Visualização**: Retângulos verdes são desenhados ao redor das faces detectadas
- **Status**: Label mostra o status da detecção ("Face detectada ✓" ou "Procurando faces...")

### 2. Classe de Reconhecimento Facial

- **Localização**: `util/ReconhecimentoFacial.java`
- **Funcionalidades**:
  - Inicialização do detector OpenCV
  - Detecção de faces em imagens
  - Desenho de retângulos ao redor das faces
  - Extração de descritores faciais
  - Conversão entre formatos de imagem

## Como Usar

### 1. Abrir a Câmera

1. Execute a aplicação
2. Navegue até a tela da câmera
3. A detecção facial será iniciada automaticamente

### 2. Visualizar Detecção

- **Faces detectadas**: Aparecem com retângulos verdes ao redor
- **Status**: Mostrado no label abaixo da imagem da câmera
- **Cores do status**:
  - Verde: Face detectada
  - Laranja: Procurando faces
  - Vermelho: Detecção indisponível

### 3. Capturar Foto

- Clique no botão "Capturar" para salvar a imagem atual
- A imagem capturada incluirá os retângulos de detecção se houver faces

## Configuração Técnica

### Dependências

- OpenCV JavaCV
- JavaFX
- Arquivos Haar Cascade (já incluídos no projeto)

### Compatibilidade Multiplataforma

- ✅ **Windows** - Totalmente suportado
- ✅ **Linux** - Totalmente suportado
- ✅ **macOS** - Totalmente suportado
- ✅ **Detecção automática** do sistema operacional
- ✅ **Correção automática** de caminhos de arquivos

### Arquivos Importantes

- `src/main/resources/opencv/haarcascades/haarcascade_frontalface_default.xml`
- `src/main/java/util/ReconhecimentoFacial.java`
- `src/main/java/controller/CameraController.java`
- `src/main/resources/com/mycompany/cpma/cameraView.fxml`

## Exemplo de Uso Programático

```java
// Criar instância do reconhecimento facial
ReconhecimentoFacial reconhecimento = new ReconhecimentoFacial();

// Inicializar o detector
reconhecimento.inicializar();

// Verificar se foi inicializado corretamente
if (reconhecimento.isInicializado()) {
    // Detectar faces em uma imagem
    BufferedImage imagem = // sua imagem
    boolean faceDetectada = reconhecimento.detectarFace(imagem);

    if (faceDetectada) {
        // Desenhar retângulos ao redor das faces
        BufferedImage imagemComRetangulos = reconhecimento.desenharRetangulosFaces(imagem);
    }
}
```

## Troubleshooting

### Problemas Comuns

1. **"Detecção facial indisponível"**

   - Verifique se o arquivo `haarcascade_frontalface_default.xml` está no local correto
   - Verifique se as dependências do OpenCV estão instaladas

2. **Câmera não funciona**

   - Verifique se a câmera está conectada e não está sendo usada por outro aplicativo
   - Verifique as permissões de acesso à câmera

3. **Performance lenta**

   - A detecção facial é processada a cada 33ms (30 FPS)
   - Para melhorar a performance, você pode ajustar o intervalo no `CameraController`

4. **Problemas específicos do Linux**

   - Certifique-se de que as bibliotecas OpenCV estão instaladas: `sudo apt-get install libopencv-dev`
   - Verifique as permissões da câmera: `sudo usermod -a -G video $USER`
   - Reinicie o sistema após adicionar o usuário ao grupo video

5. **Problemas específicos do Windows**
   - Verifique se o Visual C++ Redistributable está instalado
   - Execute como administrador se necessário

## Próximos Passos

Para expandir a funcionalidade, você pode:

1. **Implementar reconhecimento de pessoas específicas**

   - Treinar um modelo com faces conhecidas
   - Comparar faces detectadas com o banco de dados

2. **Melhorar a precisão**

   - Usar classificadores mais avançados
   - Implementar detecção de múltiplas faces

3. **Adicionar mais funcionalidades**
   - Detecção de emoções
   - Estimativa de idade
   - Detecção de gênero

## Arquivos Modificados

- ✅ `src/main/java/util/ReconhecimentoFacial.java` - Implementação completa
- ✅ `src/main/java/controller/CameraController.java` - Integração com câmera
- ✅ `src/main/resources/com/mycompany/cpma/cameraView.fxml` - Interface atualizada
- ✅ `src/main/java/controller/ExemploReconhecimentoFacial.java` - Exemplo de uso
