# Modelos DNN para Reconhecimento Facial

Este diretório contém os modelos de Deep Neural Network (DNN) para detecção e reconhecimento facial.

## Modelos Suportados

### Detecção de Faces (YuNet/MTCNN)

1. **YuNet** - Detector de faces baseado em DNN, mais robusto que Haar Cascade
   - Modelo recomendado: `yunet.onnx` ou `face_detection_yunet_2023mar.onnx`
   - Baixe em: https://github.com/opencv/opencv_zoo/tree/master/models/face_detection_yunet

### Reconhecimento Facial (Embeddings)

1. **OpenFace** - Modelo leve e eficiente
2. **FaceNet** - Modelo desenvolvido pelo Google
3. **ArcFace** - Modelo com margem angular aditiva

## Como Obter os Modelos

### Detecção de Faces (YuNet) - RECOMENDADO

1. Baixe o modelo YuNet ONNX:

   ```bash
   # YuNet 2023 (mais recente)
   wget https://github.com/opencv/opencv_zoo/raw/master/models/face_detection_yunet/face_detection_yunet_2023mar.onnx

   # Ou YuNet 2022
   wget https://github.com/opencv/opencv_zoo/raw/master/models/face_detection_yunet/face_detection_yunet_2022mar.onnx
   ```

2. Coloque o arquivo neste diretório como `yunet.onnx` ou `face_detection_yunet_2023mar.onnx`

### Reconhecimento Facial

### Opção 1: OpenFace (Recomendado)

1. Baixe o modelo OpenFace ONNX:

   ```bash
   # Modelo pré-treinado OpenFace
   wget https://github.com/opencv/opencv_extra/raw/master/testdata/dnn/opencv_face_detector_uint8.onnx
   ```

2. Coloque o arquivo neste diretório como `opencv_face_detector_uint8.onnx`

### Opção 2: FaceNet

1. Baixe um modelo FaceNet ONNX de fontes confiáveis:

   - [ONNX Model Zoo](https://github.com/onnx/models)
   - [Hugging Face](https://huggingface.co/models?search=facenet)

2. Coloque o arquivo neste diretório como `facenet.onnx`

### Opção 3: ArcFace

1. Baixe um modelo ArcFace ONNX:

   - [Modelos ArcFace](https://github.com/deepinsight/insightface)

2. Coloque o arquivo neste diretório como `arcface.onnx`

## Estrutura Esperada

```
src/main/resources/models/
├── README.md (este arquivo)
├── yunet.onnx (recomendado - detecção de faces)
├── face_detection_yunet_2023mar.onnx (alternativa)
├── facenet.onnx (opcional - reconhecimento)
└── arcface.onnx (opcional - reconhecimento)
```

## Notas Importantes

- **Detecção de Faces**: O sistema tentará usar YuNet primeiro. Se não disponível, usa Haar Cascade como fallback
- **Reconhecimento**: O sistema tentará carregar os modelos na ordem listada acima
- Se nenhum modelo de reconhecimento for encontrado, o sistema usará um método baseado em características como fallback
- Modelos ONNX são preferidos por serem portáveis e eficientes
- O tamanho típico dos modelos varia de 1MB a 50MB
- **YuNet** é significativamente mais robusto que Haar Cascade para detecção de faces

## Verificação

Após adicionar um modelo, reinicie a aplicação. Os logs mostrarão:

**Para Detecção de Faces:**

```
✅✅✅ DETECTOR DNN DE FACES CARREGADO COM SUCESSO! ✅✅✅
```

**Para Reconhecimento:**

```
✅✅✅ MODELO DNN CARREGADO COM SUCESSO! ✅✅✅
```

Se os modelos não forem encontrados, você verá:

```
⚠️ Nenhum detector DNN de faces encontrado
💡 Usando detector Haar Cascade (fallback)
```

```
⚠️ Nenhum modelo DNN encontrado nos caminhos configurados
💡 Usando método baseado em características (fallback)
```

## Performance

- **Detecção com YuNet**: Muito mais robusta que Haar Cascade, melhor em condições difíceis (iluminação, ângulos)
- **Detecção com Haar Cascade (fallback)**: Funcional, mas menos robusta
- **Reconhecimento com modelo DNN**: Maior precisão, reconhecimento mais robusto
- **Reconhecimento sem modelo DNN (fallback)**: Funcional, mas com precisão reduzida

## Licença

Certifique-se de verificar as licenças dos modelos antes de usá-los em produção.
