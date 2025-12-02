import { toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

export let gear1Color = '';
export let gear2Color = '';
export let gear3Color = '';
export let gear4Color = '';




export const nodeFunction=(imageId)=>{
    gear2Color ='';
    gear3Color ='';
    gear1Color ='';
    gear4Color = '';
};

export const node1Function=(imageId)=>{
    
          if(imageId === "s1-1"){
       
            gear2Color ='';
            gear3Color ='';
            gear1Color ='';
            gear4Color = '';
           
               
            }else if(imageId === "s1-2")
            {
         
            gear2Color ='';
            gear3Color ='';
            gear4Color = '';
            gear1Color ='progress_gear';
            toast.success("Retraining in Process", { autoClose: 3000 });
           
               
            }else if(imageId === "s1-3")
            {
                
                gear1Color ='success_gear';
                gear2Color ='';
                gear3Color ='';
                gear4Color = '';
                toast.success("Retraining completed. Sent to ML agent.", { autoClose: 3000 });
            }else if(imageId === "s1-4")
            {
           
                gear1Color ='stopGreenGear';
                
                gear2Color ='';
                gear3Color ='';
                gear4Color = '';
                toast.success("ML agent Processing.", { autoClose: 3000 });
                
            }else if(imageId === "s1-5")
            {
                
                gear1Color ='stopGreenGear';
                
                gear2Color ='';
                gear3Color ='';
                gear4Color = '';
                toast.success("Aggregation in Process.", { autoClose: 3000 });
                
            }else if(imageId === "s1-6")
            {
               
                gear1Color ='stopGreenGear';
                
                gear2Color ='';
                gear3Color ='';
                gear4Color = 'success_gear';
                toast.success("Aggregation is Completed.", { autoClose: 3000 });
               
            }else if(imageId === "s1-7")
            {
                
                gear1Color ='stopGreenGear';
                gear4Color = 'stopGreenGear';
                
               
                gear2Color ='';
                gear3Color ='';
               
                
            }else if(imageId === "s1-8")
            {
           
            gear1Color ='stopGreenGear';
            gear4Color = 'stopGreenGear';
            
           
            gear2Color ='';
            gear3Color ='';
           
                
            }else {
             
            
            gear2Color ='';
            gear3Color ='';
            gear4Color = '';
            gear1Color ='';
          
            }
            return {
            
                 gear1Color
              
            };
};

export const node2Function=(imageId)=>{
   
    if(imageId === "s2-1"){
       gear2Color ='';
        gear3Color ='';
        gear4Color = '';
        gear1Color ='';
       
        
     }else if(imageId === "s2-2")
     {
        
        
        gear2Color ='progress_gear';
        gear3Color ='';
        gear4Color = '';
        gear1Color ='';
        toast.success("Retraining in Process", { autoClose: 3000 });
        
     }else if(imageId === "s2-3")
     {
        
         gear1Color ='';
         
         gear2Color ='success_gear';
         gear3Color ='';
         gear4Color = '';
         gear1Color ='';
         toast.success("Retraining completed. Sent to ML agent", { autoClose: 3000 });
         
     }else if(imageId === "s2-4")
     {
         
         gear2Color ='stopGreenGear';
         gear3Color ='';
         gear4Color = '';
         gear1Color ='';
         toast.success("ML agent Processing.", { autoClose: 3000 });
     }else if(imageId === "s2-5")
     {
        
         gear1Color ='';
       
         gear2Color ='stopGreenGear';
         gear3Color ='';
         gear4Color = '';
         toast.success("Aggregation in Process.", { autoClose: 3000 });

     }else if(imageId === "s2-6")
     {
       
         gear1Color ='';
        
         gear2Color ='stopGreenGear';
         gear3Color ='';
         gear4Color = 'success_gear';
         toast.success("Aggregation is Completed.", { autoClose: 3000 });
        
     }else if(imageId === "s2-7")
     {
        
         gear1Color ='';
        
         gear4Color = 'stopGreenGear';
         gear2Color ='stopGreenGear';
         gear3Color ='';
         
         
     }else if(imageId === "s2-8")
     {
     
     gear2Color ='stopGreenGear';
    
     
         
     }else {
      
        gear4Color = '';
        gear2Color ='';
        gear3Color ='';
        gear1Color ='';
       
     }
           return {
           
            gear2Color
            
        };
};

export const node3Function=(imageId)=>{
   
    if(imageId === "s3-1"){
        
        gear2Color ='';
        gear3Color ='';
        gear1Color ='';
        gear4Color = '';
        
        
     }else if(imageId === "s3-2")
     {

         
        gear2Color ='';
        gear3Color ='progress_gear';
        gear1Color ='';
        gear4Color = '';
        toast.success("Retraining in Process", { autoClose: 3000 });
        
     }else if(imageId === "s3-3")
     {
        
         gear1Color ='';
        
         gear2Color ='';
         gear3Color ='success_gear';
         gear4Color = '';
         gear1Color ='';
         toast.success("Retraining completed. Sent to ML agent", { autoClose: 3000 });
     }else if(imageId === "s3-4")
     {
         
         gear1Color ='';
         
         gear2Color ='';
         gear3Color ='stopGreenGear';
         gear4Color = '';
         toast.success("ML agent Processing.", { autoClose: 3000 });
       
     }else if(imageId === "s3-5")
     {
         
         gear1Color ='';
        
         gear2Color ='';
         gear3Color ='stopGreenGear';
         gear4Color = '';
         toast.success("Aggregation in Process.", { autoClose: 3000 });
        
     }else if(imageId === "s3-6")
     {
         
         gear1Color ='';
        
         gear2Color ='';
         gear3Color ='stopGreenGear';
         gear4Color = 'success_gear';
         toast.success("Aggregation is Completed.", { autoClose: 3000 });
         
     }else if(imageId === "s3-7")
     {
         
      
         gear1Color ='';
        
         gear2Color ='';
         gear3Color ='stopGreenGear';
         gear4Color = 'stopGreenGear';
        
         
     }else if(imageId === "s3-8")
     {
     
     gear3Color ='stopGreenGear';
     gear4Color = 'stopGreenGear';
     gear2Color ='';
     gear1Color ='';
         
     }else {
      
        
        gear2Color ='';
        gear3Color ='';
        gear1Color ='';
        
        gear4Color = '';
        
     }
           
           return{
          
            gear3Color
           
        }; 
};





